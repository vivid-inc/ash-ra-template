(ns io.github.plexus.cljs-test-example.kaocha-hooks
  (:require [clojure.java.browse :as browse]
            [clojure.java.shell :as sh]
            [shadow.cljs.devtools.api :as shadow-api]
            [shadow.cljs.devtools.server :as shadow-server]
            [shadow.cljs.devtools.server.runtime :as shadow-runtime]
            [kaocha.cljs2.funnel-client :as funnel]))

(defn spawn
  "Start a process, connecting its stdout/stderr to the parent so we see what's
  going on. Returns the Process object so you can call .pid, .destroy,
  .destroyForcibly."
  [args opts]
  (let [builder (doto (ProcessBuilder. args)
                  (.inheritIO))
        environment (.environment builder)]
    (when-let [env (:env opts)]
      (doseq [[k v] env]
        (.put environment k v)))
    (.start builder)))

(defn ensure-funnel! []
  ;; If funnel is already running then this is a no-op
  (sh/sh "bin/funnel_wrapper" "-vv" "--daemonize"))

(defn ensure-shadow-instance! []
  (when (nil? @shadow-runtime/instance-ref)
    (shadow-server/start!)
    (loop []
      (Thread/sleep 250)
      (when (nil? @shadow-runtime/instance-ref)
        (recur)))))

(defn shadow-dev-build! [testable]
  (shadow-api/compile (:shadow/build testable)))

(defn pre-load [testable config]
  (ensure-funnel!)
  (ensure-shadow-instance!)
  (shadow-dev-build! testable)
  ;; Always return the first argument from Kaocha hooks
  testable)

(defn launch-browser-and-wait [{:funnel/keys [conn]
                                :kaocha.cljs2/keys [timeout]}]
  ;; Both these calls ask Funnel if it has any clients that look like they are
  ;; the ones we would want to talk to, in particular it sends this query to Funnel:
  ;;
  ;; {:lambdaisland.chui.remote? true
  ;;  :working-directory (.getAbsolutePath (io/file ""))}
  ;;
  ;; Remember that Funnel is fully symmetrical, Kaocha-cljs2 (JVM) is just
  ;; another client, as are Chui-remote (JS) clients. We only want chui-remote
  ;; clients, and in particular we want ones which CLJS build was triggered in
  ;; the same project directory that we are in, so we don't accidentally connect
  ;; to another project's browser tab.
  (when (empty? (funnel/list-clients conn))
    (browse/browse-url "http://localhost:8000"))
  (funnel/wait-for-clients conn (if timeout {:timeout timeout})))
