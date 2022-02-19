(ns kev.blog.posts
  "wrapper around the posts ns to create all the posts and make searchable"
  (:require
   [re-frame.loggers :refer [console]]
   ["@mui/material" :as mui]))

;; TODO switch to defonce
(defonce posts* (atom {}))

(defn add-post! [{::keys [post-id] :as post}]
  (swap! posts* assoc post-id post))

(defn all-posts []
  @posts*)

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
