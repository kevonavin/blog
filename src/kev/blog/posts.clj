(ns kev.blog.posts
  (:refer-clojure :exclude [-])
  (:require
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [kev.blog.posts :as posts]))

(defn date-str? [s]
  (re-matches #"\d{4}-\d{2}-\d{2}" s))

(symbol? \.)

(defn collect-parts [forms]
  (->> forms
       (reduce
        (fn [aggregated item]
          (cond
            (or (symbol? item)
                (string? item))
            (if (-> aggregated first string?)
              (let [into-str (first aggregated)]
                (->> aggregated
                     rest
                     (cons (str into-str " " (name item)))))
              (cons (name item) aggregated))

            (and (char? item)
                 (-> aggregated first string?))
            (->> aggregated
                 rest
                 (cons (str (first aggregated) item)))

            :else
            (cons item aggregated)))
        ())
       (reverse)
       ;; hack to get spacing around inline components
       (map (fn [x]
                (if (and (string? x)
                       ;; punctuation should be next to things
                         (not (#{\. \? \!} (first x))))
                  (str " " x " ")
                  x)))))

(s/def ::post-id string?)
(s/def ::title string?)
(s/def ::created date-str?)
(s/def ::modified date-str?)
(s/def ::post-meta (s/keys :req [::post-id
                                 ::title
                                 ::created]
                           :opt [::modified]))

(defmacro defpost [post-meta & body]
  (when-not (s/valid? ::post-meta post-meta)
    (throw (ex-info "invalid metadata"
                    {:explained (s/explain-data ::post-meta post-meta)})))
  ;; TODO use reframe db for this instead
  `(posts/add-post! (-> ~post-meta
                        (assoc ::body
                               [posts/post-content
                                ~post-meta
                                ~@(collect-parts body)]))))

;; should be in a list with those below it
(defmacro - [& body]
  `(posts/bullet
    ~@(collect-parts body)))
