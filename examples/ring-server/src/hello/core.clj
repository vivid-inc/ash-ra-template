; Copyright 2022 Vivid Inc.
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;    https://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns hello.core
    (:require
      [ring.adapter.jetty]
      [ring.middleware.params]
      [vivid.art :as art]))

; ART template
(def ^:const greeting-template
  "<% (require 'clojure.string) %>
  Wait! Allow me to first guess your nickname.
  Is it perchance, <%= (clojure.string/upper-case nickname) %>?")

(defn handler [request]
      (let [nickname (get-in request [:params "nickname"] "[redacted]")]
           {:status  200
            :headers {"Content-Type" "text/html"}
            ; Renders the ART template to as the response body.
            ; #'nickname was parsed from the HTTP request by Ring;
            ; we pass it through to the template as a binding.
            :body    (art/render greeting-template {:bindings {'nickname nickname}})}))

(def app-handler
  (-> handler
      (ring.middleware.params/wrap-params {:encoding "UTF-8"})))

(defn -main
      [& args]
      (ring.adapter.jetty/run-jetty app-handler {:port 3000}))
