(ns kev.blog.posts
  (:refer-clojure :exclude [-])
  (:require
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [kev.blog.posts :as posts]))

(defn date-str? [s]
  (re-matches #"\d{4}-\d{2}-\d{2}" s))

(defn collect-parts [forms]
  (reverse
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
        :else
        (cons item aggregated)))
    ()
    forms)))

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

(let [post-subdir "src/kev/blog/posts/"
      s "src/kev/blog/posts/test.cljs"]
  (re-matches #".*/([a-z_]+)\.cljs" s))

(defmacro require-post-namespaces-ns-form! [ns]
  (let [post-subdir "src/kev/blog/posts/"
        ns-syms (->> (io/file post-subdir)
                     (file-seq)
                     (map str)
                     (keep (fn [s]
                             (let [[_ ns-end] (re-matches #".*/([a-z_]+)\.cljs" s)]
                               (when ns-end
                                 (symbol (str "kev.blog.posts." ns-end)))))))]
    `(ns ~ns (:require ~@ns-syms))))
