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

(ns vivid.art.cli
  "Internal API."
  (:require
    [clojure.spec.alpha :as s]
    [clojure.tools.deps.alpha.specs]
    [vivid.art]))

(def ^:const art-filename-suffix ".art")
(def ^:const art-filename-suffix-regex #"\.art$")

; Clojure dependency maps, required to render a given template
(s/def :vivid.art/dependencies :clojure.tools.deps.alpha.specs/deps-map)

; TODO How to incorporate this spec to the one that already exists?
#_(s/fdef vivid.art/render
          :args (s/cat :t :vivid.art/template
                       :o (s/? (s/keys :opt-un [:vivid.art/dependencies]))))
