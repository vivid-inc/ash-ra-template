; Copyright 2020 Vivid Inc.
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

(ns vivid.art.enscript
  (:require
    [clojure.string]))

(def ^:const prelude ["(ns user)"
                      "(def ^java.lang.StringBuilder __vivid__art__sb (new java.lang.StringBuilder))"
                      "(defn emit ([]) ([v] (.append user/__vivid__art__sb v) nil))"])
(def ^:const coda ["(.toString user/__vivid__art__sb)"])

(defn define-bindings
  [bindings]
  (vec (for [[k v] bindings] (format "(def %s %s)"
                                     (pr-str k)
                                     (pr-str v)))))

(defn define-render-context
  [render-context]
  #_(println "** define-render-context " (binding [*print-namespace-maps* false]
                                         (pr-str render-context)))
  [(format "(def ^:dynamic *render-context* )"
           ; Disabling *print-namespace-maps* prevents {:a/b 1} from being
           ; printed as "#:a{:b 1}".
           ; Referencing https://clojure.atlassian.net/browse/CLJ-2469
           "nil" #_(binding [*print-namespace-maps* false]
             (pr-str render-context)))])

(defn enscript
  [forms render-context]
  (let [bindings (get-in render-context [:render-options :bindings])]
    (as-> [prelude
           (define-render-context render-context)
           (define-bindings bindings)
           forms
           coda] s
          (remove empty? s)
          (interleave s (repeat ""))
          (flatten s)
          (clojure.string/join "\n" s))))
