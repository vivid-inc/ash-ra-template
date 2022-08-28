; Run either with
;     $ lein ring server
; or in a REPL:
;     $ lein repl
;     hello.core=> (ring.adapter.jetty/run-jetty app-handler {:port 3000})
;
; and then visit
;     http://localhost:3000/?nickname=beatrix

(defproject art-example-ring-server "1.0.0-SNAPSHOT"
            :description "Minimal demonstration rendering responses in Ring handlers using ART templates."
            :dependencies [[org.clojure/clojure     "1.10.0"]
                           [ring/ring-core          "1.9.5"]
                           [ring/ring-jetty-adapter "1.9.5"]
                           [net.vivid-inc/art               "0.7.0"]]
            :main hello.core
            :plugins [[lein-ring "0.12.6"]]
            :ring {:handler hello.core/app-handler})
