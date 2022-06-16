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

(ns vivid.art.failure
  (:require
    [clojure.spec.alpha :as s]
    [vivid.art.specs]))

(defn failure?
  "When ART fails to render a template, instead of the template output,
  it produces a value that describes the failure. Use this function to
  discriminate a failure from regular template output."
  [result]
  (when (s/valid? :vivid.art/failure result)
    result))

(defn make-failure
  "Makes an ART failure data structure describing the type of failure,
  the input that triggered the failure, and information about the cause."
  [failure-type cause template]
  {:failure-type failure-type
   :cause        cause
   :template     template})
(s/fdef make-failure
        :ret :vivid.art/failure)
