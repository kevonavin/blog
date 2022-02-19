(ns kev.blog.posts
  "wrapper around the posts ns to create all the posts and make searchable"
  (:require
   [re-frame.loggers :refer [console]]
   [clojure.spec.alpha :as s :include-macros true]
   [clojure.string :as str]
   ["@mui/material" :as mui]))

(declare hiccup->text)

;; TODO switch to defonce
(defonce posts* (atom {}))

(defn add-post! [{::keys [body post-id] :as post}]
  (swap! posts*
         assoc post-id (assoc post
                              ::raw-text (hiccup->text body))))

(defn all-posts []
  @posts*)

;;;;;;;;;;;;;;;;;; utils ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::react (s/cat :start #{:>}
                      :react-elem any?
                      :props (s/? map?)
                      :childs (s/* (complement seq?))
                      :child-seq (s/? seq?)))

(s/def ::fn-leading (s/cat :start fn?
                      :props (s/? map?)
                      :childs (s/* (complement seq?))
                      :child-seq (s/? seq?)))

(s/def ::keyword-leading (s/cat :start keyword?
                                :props (s/? map?)
                                :childs (s/* (complement seq?))
                                :child-seq (s/? seq?)))

(s/def ::hiccup (s/or :react ::react
                      :fn ::fn-leading
                      :kw ::keyword-leading
                      :string string?))

(defn hiccup->text [hiccup]
  (when-not (s/valid? ::hiccup hiccup)
    (s/explain ::hiccup hiccup)
    (throw (ex-info "failed to conform hiccup to extract text!!"
                    {:explained (s/explain-str ::hiccup hiccup)})))
  (let [[type {:keys [child-seq childs] :as parsed}] (s/conform ::hiccup hiccup)]
    (cond
      (= type :string) parsed
      :else (->> (concat childs child-seq)
                 (filter some?)
                 (map hiccup->text)
                 (str/join " ")))))

(defn ->child-seq [text->elem chs]
  (->> chs
       (map (fn [x]
              (if (string? x)
                (text->elem x)
                x)))
       (map-indexed (fn [i x]
                 (with-meta x {:key i})))))

(defn typog [var s]
  [:> mui/Typography (if (map? var) var {:variant var}) s])


;;;;;;;;;;;;;;;;;;;;;; post stuff;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;
(defn post-content [{::keys [created title] :as props} & children]
  [:> mui/Box
   {:sx {:width "100%"
         :display "flex"
         :justify-content "center"}}
   [:> mui/Box
    {:sx {:max-width "70ch"
          :width "100%"}}
    [typog "h4" title]
    [typog {:variant "body2"
            :color "text.secondary"} (str "created: " created)]
    [:> mui/Box {:sx {:mb 1.5}}]
    [typog {:variant "body1"
            :component "div"}
     (->child-seq #(do [:span %])
                  children)]]])

(defn bullet [& children]
  [:> mui/Box
   {:sx {:ml "4ch"}}
   [:> mui/Box
    {:sx {:ml "-2ch"
          :position "absolute"}}
    [:> mui/Typography {:variant "body1"} "\u2022"]]
   (->child-seq #(do [:> mui/Typography {:variant "body1"
                                                   :sx {}}
                      %])
                children)])
