;; This is identical to lambdaisland.chui.shadow.browser-runner, but it's not a
;; bad idea to inline that into your project so you have some control over it.

(ns io.github.plexus.cljs-test-example.shadow-runner
  "Runner namespace to be used with shadow-cljs's :browser-test target."
  {:dev/always true}
  (:require [goog.dom :as gdom]
            [lambdaisland.chui.runner :as runner]
            [lambdaisland.chui.ui :as ui]
            [lambdaisland.chui.test-data :as test-data]
            [lambdaisland.glogi :as log]
            [lambdaisland.glogi.console :as glogi-console]))

(glogi-console/install!)

(log/set-levels
 '{:glogi/root :debug
   lambdaisland :all
   lambdaisland.chui.interceptor :error})

(defn start []
  ;; This is a macro which finds all test vars, and stores their metadata inside
  ;; the build, so chui-core can pick that up
  (test-data/capture-test-data!)
  (js/window.requestIdleCallback #(ui/run-tests)))

(defn stop [done]
  ;; Unique thing about Chui is that it can actually stop a ClojureScript build
  ;; that's in progress, although only at the next interceptor point, e.g.
  ;; before/after a test var, fixture, or async test block
  (runner/terminate! done))

(defn ^:export init []
  ;; This is called only once. We render the Chui-ui interface. This is not
  ;; necessary, you don't need the chui-ui (e.g. you wouldn't have it if this
  ;; was a node build), it just gives you another interface onto the test
  ;; runner (chui-core). Kaocha-cljs2 (via funnel and chui-remote) gives you
  ;; another interface over *the same test run*
  (let [app (gdom/createElement "div")]
    (gdom/setProperties app #js {:id "chui-container"})
    (gdom/append js/document.body app)
    (ui/render! app))
  (start))
