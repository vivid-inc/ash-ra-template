; Copyright 2024 Vivid Inc. and/or its affiliates.
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

(ns ^:internal-api vivid.art.enscript
  (:require
   [clojure.string]))

(set! *warn-on-reflection* true)

(defn prelude [ns-sym]
  [(str "(ns " ns-sym ")")
   ; Provide (render) to the template evaluation environment
   "(require '[vivid.art :refer [render]])"

   ; Generally sorted alphabetically
   "(def ^java.lang.StringBuilder __vivid__art__sb (new java.lang.StringBuilder))"
   "(defn __vivid__art__emit [^java.lang.StringBuilder sb & more] (doseq [m more] (.append sb m)))"
   "(defmacro block [& body] `(let [__vivid__art__sb# (new java.lang.StringBuilder) __vivid__art__emit# (partial __vivid__art__emit __vivid__art__sb#)] (binding [emit __vivid__art__emit#] ~@body (.toString __vivid__art__sb#))))"
   (str "(def ^:dynamic emit (partial __vivid__art__emit " ns-sym "/__vivid__art__sb))")
   "(defn yield [k] (let [rc vivid.art/*render-context*] (get-in rc [:bindings k] \"\")))"])

(defn coda [ns-sym]
  [(str "(.toString " ns-sym "/__vivid__art__sb)")])

(defn def-bindings
  [bindings]
  (vec (for [[k v] bindings]
         (let [quote-value? (get (meta v) :quote-value?)]
           (format "(def %s %s%s)"
                   ; Non-symbols may lead to an invalid (def)
                   (pr-str (symbol k))
                   (if quote-value? "'" "")
                   (pr-str v))))))

(defn enscript
  [forms bindings]
  (let [ns-sym (get @(ns-resolve (find-ns 'vivid.art) '*render-context*) :ns)]
    (as-> [(prelude ns-sym)
           (def-bindings bindings)
           forms
           (coda ns-sym)] s
      (remove empty? s)
      (interleave s (repeat ""))
      (flatten s)
      (clojure.string/join "\n" s))))
