(ns kev.blog.dev
  (:require
   [nrepl.cmdline]
   [clojure.pprint]
   [clojure.edn :as edn]
   [clojure.core.match :refer [match]]
   [clojure.walk]
   [lambdaisland.classpath :as licp]
   [shadow.cljs.devtools.api :as shadow.api]
   [shadow.cljs.devtools.cli-actual]
   [shadow.cljs.devtools.server :as shadow.server]))

(comment

  shadow.cljs.devtools.api
  (licp/update-classpath! {:aliases [:cljs]})

  (shadow.cljs.devtools.api/stop-worker :app)
  (shadow.cljs.devtools.api/watch :app)
  )

(defn something [& args]
  (prn "did something" args))

(defn build
  "release-opts will by passsed into shadow. These are essentially the same
  as shadow cli options."
  [{:keys [app release-opts]}]
  ;; TODO
  ;; collect all posts together
  ;; update relative paths in index.html
  (shadow.cljs.devtools.api/release! app release-opts))

(defn dev-repl [{:keys [cider-args]}]
  (shadow.server/start!)
  (apply nrepl.cmdline/-main cider-args))
