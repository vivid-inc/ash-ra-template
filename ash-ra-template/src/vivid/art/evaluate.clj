; Copyright 2019 Vivid Inc.
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

(ns vivid.art.evaluate
  (:require
    [clojure.string]
    [vivid.art.embed :as embed]))

(def ^:const prelude ["(ns user)"])
(def ^:const interlude ["(def ^java.lang.StringBuilder __vivid__art__sb (new java.lang.StringBuilder))"
                        "(defn emit ([]) ([v] (.append user/__vivid__art__sb v) nil))"])
(def ^:const coda ["(.toString user/__vivid__art__sb)"])

(defn define-bindings
  [bindings]
  (vec (for [[k v] bindings] (format "(def %s %s)"
                                     (pr-str k)
                                     (pr-str v)))))

(defn evaluate
  [forms bindings dependencies]
  (let [code (->> (interpose [""]
                             [prelude
                              (define-bindings bindings)
                              interlude
                              forms
                              coda])
                  (flatten)
                  (clojure.string/join "\n"))]
    (embed/eval-in-one-shot-runtime code dependencies)))
