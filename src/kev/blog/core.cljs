(ns kev.blog.core
  (:require
   [com.wsscode.async.async-cljs :as ac]
   [promesa.core :as p]
   [clojure.string :as str]
   [re-frame.core :as rf]
   [re-frame.loggers :refer [console]]
   [re-frame.db :as rf.db]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [kev.blog.posts :as posts]
   [reitit.frontend.easy :as rfe]
   [reitit.frontend]
   ["@mui/material/styles" :refer [createTheme]]
   ["@mui/material" :as mui]
   ["fuzzysort" :as fuzzysort]
   [net.cgrand.xforms :as x]
   [kev.blog.posts.test]))

(declare render-page!)

(defn ^:export init []
  (re-frame.loggers/set-loggers!
   {:debug (fn [& args] (apply js/console.debug (map clj->js args)))
    :log   (fn [& args] (apply js/console.log (map clj->js args)))
    :warn   (fn [& args] (apply js/console.warn (map clj->js args)))
    :error   (fn [& args] (apply js/console.error (map clj->js args)))
    :group   (fn [& args] (apply js/console.group (map clj->js args)))
    :groupEnd   (fn [& args] (apply js/console.groupEnd (map clj->js args)))})
  (render-page!))

(defn partition-str [s n step]
  (let [len (count s)]
    (loop [cur 0
           strs  []]
      (if (>= cur len)
        strs
        (recur (+ cur step)
               (conj strs
                     (subs s cur (+ n cur))))))))

(rf/reg-event-db
 :blog/init
 (fn [db]
   (assoc db
          :blog/search-targets
          (clj->js
           (into []
                 (x/for [[_ {::posts/keys [raw-text title post-id]}] _
                         :let [n 50
                               step 20]
                         [i tidbit] (map-indexed vector (partition-str raw-text n step))]
                   {:id    post-id
                    :raw   raw-text
                    :start (* i step)
                    :title title
                    :text  (fuzzysort/prepare tidbit)})
                 (posts/all-posts))))))

(rf/reg-sub
 :blog/search-targets
 :blog/search-targets)

(rf/reg-event-db
 :posts/set
 (fn [db [_ id]]
   (assoc db :page/current-id id)))

(rf/reg-sub
 :page/current
 (fn [db _sub-vec]
   (:page/current-id db)))

(defn rand-string [len]
  (apply str (repeatedly len #(rand-int 10))))

(defn fuzzysort-result->hiccup [result]
  (let [separator (rand-string 10)
        separated (fuzzysort/highlight result separator separator)]
    (->> (str/split separated (js/RegExp separator))
         (partition 3 2 '("" ""))
         (mapcat (fn [[start highlighted rest]]
                   (list start
                         [:mark highlighted]
                         rest)))
         (dedupe)
         (filter (complement (comp empty? posts/hiccup->text))))))

(defn search-box []
  (r/with-let [popover-anchor (r/atom nil)
               search-results (r/atom nil)
               before-after-len 40]
    [:> mui/Box
     {:on-change (fn [e]
                   (swap! popover-anchor #(or % (.-target e)))
                   (let [search (.. e -target -value)
                         results (->> (fuzzysort/go search
                                                    @(rf/subscribe [:blog/search-targets])
                                                    (clj->js {:limit 500
                                                              :threshold -100
                                                              :allowTypo true
                                                              :keys [:title :text]}))
                                      (map (fn [{title-result 0 text-result 1 :as obj}]
                                             (let [{:keys [id title start raw]} (js->clj (aget obj "obj")
                                                                                         :keywordize-keys true)
                                                   highlight-text (fuzzysort-result->hiccup text-result)
                                                   text-len   (->> highlight-text
                                                                   (vector :div)
                                                                   posts/hiccup->text
                                                                   count)]
                                               {:post-id id
                                                :text-highlight
                                                (concat
                                                 ["..." (subs raw (- start before-after-len) start)]
                                                 highlight-text
                                                 [(subs raw start (+ start text-len before-after-len)) "..."])
                                                :title-highlight (or (seq (fuzzysort-result->hiccup title-result))
                                                                     title)})))
                                      (group-by :post-id)
                                      (map (fn [[_ [best & _]]]
                                             (console :log "best" best)
                                             best))
                                      (take 20))]
                     (reset! search-results
                             results)))}
     [:> mui/TextField
      {:variant "standard"
       :size "small"
       :label "search"}]
     [:> mui/Popover
      {:anchor-origin {:vertical "bottom"
                       :horizontal "center"}
       :sx {:width "100%"}
       :id "simple-popover"
       :disable-auto-focus true
       :on-close #(reset! popover-anchor nil)
       :anchor-el @popover-anchor
       :open (boolean (and @popover-anchor (seq @search-results)))}
      [:> mui/List
       {:sx {:width "100%"}}
       (map-indexed
        (fn [i {:keys [text-highlight title-highlight post-id]}]
          ^{:key i}
          [:> mui/ListItemButton
           {:sx {}
            :on-click (fn [_]
                        (reset! popover-anchor nil)
                        (rf/dispatch [:posts/set post-id]))}
           [:> mui/Box
            {:sx {:m 1}}
            [:> mui/Typography
             {:variant "h5"
              :component "div"}
             (posts/->child-seq #(do [:span %])
                                title-highlight)]
            [:> mui/Typography
             {:gutter-bottom true
              :component "div"
              :variant "subtitle1"}
             (posts/->child-seq #(do [:span %])
                                text-highlight)]]])
        @search-results)]]]))

(defn header []
  [:<>
   [:> mui/Grid
    {:container true
     :justify-content "space-between"
     :sx {:width "100%"}}
    [:> mui/Grid
     {:item true
      :sx {:m 1}}
     [:> mui/Link
      {:href (rfe/href ::root)
       :underline "hover"
       :color "inherit"
       :variant "h4"}
      "Kevin's Blog"]]
    [:> mui/Grid
     {:item true}
     [search-box]]]
   [:> mui/Divider {:variant "middle"
                    :sx {:mb 2}}]])

(defn post-link [{pid ::posts/post-id
                  title ::posts/title}]
  [:a {:href  (rfe/href ::post {:id pid})} title])

(defn post []
  (let [post-id @(rf/subscribe [:page/current])]
    (::posts/body (get (posts/all-posts) post-id [:div "not found"]))))

;; currently, we don't have anything set up
;; e.g. bg color and most text color doesn't rely on the theme
(def theme (createTheme
            (clj->js
             ;; see https://fonts.google.com/ for options
             {:typography {:fontFamily "Tinos"}
              :palette {:mode :light}})))

(defn ^:dev/after-load render-page! []

  (rfe/start!
   (reitit.frontend/router
    [["/"
      {:name ::root
       :handle (fn [_] (rf/dispatch [:posts/set "home"]))}]

     ["/:id"
      {:name ::post
       :handle (fn [m] (rf/dispatch [:posts/set (-> m :path-params :id)]))}]]
    {:data {}})
   (fn [match _history]
     (some-> match :data :handle (apply [match])))
   {:use-fragment true})

  (posts/add-post!
   {::posts/post-id "home"
    ::posts/body
    [:> mui/Box {:m 1}
     (->> (posts/all-posts)
          (map second)
          (filter #(not= "home" (::posts/post-id %)))
          (sort-by ::posts/created)
          (map (fn [post]
                 [:<>
                  [post-link post]
                  [:div]]))
          (posts/->child-seq #(do [:> mui/Typography {:variant "body1"
                                                      :sx {}}
                                   %])))]})

  (rf/dispatch-sync [:blog/init])

  (rdom/render
   [:> mui/ThemeProvider
    {:theme theme}
    [:div
     [header]
     [post]]]
   (js/document.querySelector "#app")))
