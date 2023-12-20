; Copyright 2023 Vivid Inc. and/or its affiliates.
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     https://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

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
                           [net.vivid-inc/art               "0.7.1"]]
            :main hello.core
            :plugins [[lein-ring "0.12.6"]]
            :ring {:handler hello.core/app-handler})
