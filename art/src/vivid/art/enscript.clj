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

(defn prelude [sb-identifier]
  ["(ns user)"
   (str "(def ^java.lang.StringBuilder " sb-identifier " (new java.lang.StringBuilder))")
   (str "(defn emit ([]) ([& more] (doseq [m more] (.append user/" sb-identifier " m)) nil))")])
(defn coda [sb-identifier]
  [(str "(.toString user/" sb-identifier ")")])

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
  (let [sb-identifier (gensym '__vivid__art__sb)]
    (as-> [(prelude sb-identifier)
           (def-bindings bindings)
           forms
           (coda sb-identifier)] s
          (remove empty? s)
          (interleave s (repeat ""))
          (flatten s)
          (clojure.string/join "\n" s))))
