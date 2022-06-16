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

(ns vivid.art.specs
  "Clojure Spec definitions for Ash Ra Temple data structures.
  All definitions exposed through ART's API plus all key
  definitions are namespaced."
  (:require
    [clojure.spec.alpha :as s]))


; ART templates

(s/def :vivid.art/template (s/nilable string?))


; Bindings

(s/def :vivid.art/bindings map?)


; Template delimiter definitions

(s/def ::delimiter (s/and string? seq))

(s/def ::begin-forms ::delimiter)
(s/def ::end-forms ::delimiter)
(s/def ::begin-eval ::delimiter)
(s/def ::end-eval ::delimiter)

(s/def :vivid.art/delimiters
  (s/keys :opt-un [::begin-forms ::end-forms
                   ::begin-eval ::end-eval]))



; Failure descriptors

(s/def ::failure-type keyword?)
(s/def ::cause coll?)

(s/def :vivid.art/failure
  (s/keys :req-un [::failure-type ::cause ::template]))


; To which phase in the (render) dataflow will it proceed to?
; Useful for diagnostics and operational insight.

(def ^:const render-phases
  "NOTE: These values are unstable and subject to change."
  '(:parse :translate :enscript :evaluate))
(s/def :vivid.art/render-phase (into #{} render-phases))

(defn to-phase?
      "Given an unrecognized current-phase, evaluates to false,
      otherwise given an unrecognized to-phase, evaluates to true."
      [current-phase to-phase]
      (let [plan (-> #{}
                     (into (take-while #(not= to-phase %) render-phases))
                     (conj to-phase))]
           (contains? plan current-phase)))
(s/fdef to-phase?
        :args (s/cat :current-phase :vivid.art/render-phase
                     :to-phase :vivid.art/render-phase)
        :ret (s/nilable boolean?))
