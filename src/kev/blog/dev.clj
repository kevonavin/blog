(ns kev.blog.dev
  (:require
   [nrepl.cmdline]
   [clojure.pprint]
   [clojure.edn :as edn]
   [clojure.core.match :refer [match]]
   [clojure.walk]
[clojure.string :as str]
   [clojure.java.io :as io]
   [lambdaisland.classpath :as licp]
   [shadow.cljs.devtools.api :as shadow.api]
   [shadow.cljs.devtools.cli-actual]
   [shadow.cljs.devtools.server :as shadow.server]
   [shadow.cljs.devtools.server.nrepl :as shadow.nrepl]))


(comment

  (defn Y [f]
    ((fn [g] (g g))
     (fn [h]
       (fn [& args]
         (apply f (h h) args)))))

  shadow.cljs.devtools.api
  (licp/update-classpath! {:aliases [:cljs]})

  (shadow.cljs.devtools.api/stop-worker :app)
  (shadow.cljs.devtools.api/watch :app)
  (shadow.cljs.devtools.api/compile :app)
  )

(defn collect-posts! []
  (->> "./src/kev/blog/posts"
      io/file
      file-seq
      (map str)
      (filter #(re-matches #".*\.cljs$" %))
      (map (fn [fname]
             (let [[_ found] (re-find #"^./src/(.*).cljs$" fname)]
               (-> found
                   (str/replace #"/" ".")
                   (str/replace #"_" "-")))))))

(defn generate-posts-file! []
  (let [gen-file "./src/kev/blog/generated_posts.cljs"
        content (prn-str
                 `(~'ns kev.blog.generated-posts
                        (:require ~@(cons ['kev.blog.posts] (map #(do [(symbol %)]) (collect-posts!))))))
        current-content (slurp gen-file)]
    (when (not= current-content content)
      (println "generating file, " gen-file)
      (spit gen-file content))))

(defn compile-posts
  {:shadow.build/stage :configure}
  [build-state & args]
  (generate-posts-file!)
  build-state)

(defn build
  "release-opts will by passsed into shadow. These are essentially the same
  as shadow cli options."
  [{:keys [app release-opts]}]
  (shadow.cljs.devtools.api/release! app release-opts))

(defn dev-repl [{:keys [cider-args]}]
  (println "starting server")
  (shadow.server/start!)
  (println "watching app")
  (shadow.cljs.devtools.api/watch :app)
  (println "starting nrepl")
  (apply nrepl.cmdline/-main cider-args))
