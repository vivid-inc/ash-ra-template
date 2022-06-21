; Copyright 2022 Vivid Inc. and/or its affiliates.
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

(defn prelude [ns-sym]
  [(str "(ns " ns-sym ")")
   "(def ^java.lang.StringBuilder __vivid__art__sb (new java.lang.StringBuilder))"
   (str "(defn ^:dynamic emit ([]) ([& more] (doseq [m more] (.append " ns-sym "/__vivid__art__sb m)) nil))")

   ; TODO Move the following to vivid.art source file?
   "(require '[vivid.art])"
   "(declare blocks)"
   ; TODO Is (wrap-in) actually (vivid.art/render :blocks ...) ?
   "(defn wrap-in [template & {:keys [with]}] (vivid.art/render template {:bindings {'blocks with}}))"
   "(defn yield [k] (when (bound? #'blocks) (get blocks k)))"
   "(defmacro block [& body] `(let [sb# (new java.lang.StringBuilder)] (binding [emit #(.append sb# %)] ~@body (.toString sb#))))"])

(defn coda [ns-sym]
  [(str "(.toString " ns-sym "/__vivid__art__sb)")])

(defn def-bindings
  [bindings]
  (vec (for [[k v] bindings]
         (let [quote-value? (get (meta v) :quote-value?)]
           (format "(def %s %s%s)"
                   (pr-str k)
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
