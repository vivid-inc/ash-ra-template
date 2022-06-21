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

(ns vivid.art
  "Ash Ra Template public API."
  (:require
    [clojure.spec.alpha :as s]
    [farolero.core :as farolero]
    [vivid.art.delimiters]
    [vivid.art.enscript :refer [enscript]]
    [vivid.art.evaluate :refer [evaluate]]
    [vivid.art.failure :refer [make-failure]]
    [vivid.art.parse :refer [parse]]
    [vivid.art.specs :refer [to-phase?]]
    [vivid.art.xlate :refer [translate]]))

(def ^:dynamic *render-context* nil)

(def ^:const default-delimiters-name "lispy")
(def ^:const default-delimiters (var-get
                                  (ns-resolve 'vivid.art.delimiters
                                              (symbol default-delimiters-name))))

(def ^:const failure? vivid.art.failure/failure?)

(def ^:const render-phases
  "Phases of the rendering process. Note: Unstable until version 1.0."
  vivid.art.specs/render-phases)
(def ^:const default-to-phase (last render-phases))

(defn render
  "Renders an input string containing Ash Ra Template (ART) -formatted content
  to an output string."
  ([^String template] (render template {}))
  ([^String template
    {:keys [bindings delimiters to-phase]
     :or   {bindings {} delimiters default-delimiters to-phase default-to-phase}}]
   (when template
     (let [render* #(cond-> template
                        (to-phase? :parse     to-phase) (parse delimiters)
                        (to-phase? :translate to-phase) (translate)
                        (to-phase? :enscript  to-phase) (enscript bindings)
                        (to-phase? :evaluate  to-phase) (evaluate))]
       (with-bindings {#'vivid.art/*render-context*
                       {:ns (gensym 'vivid-art-user-)}}
        (farolero/handler-case (render*)
                              (:vivid.art/parse-error [_ details]
                                (make-failure :parse-error details template))))))))
(s/fdef render
        :args (s/cat :t :vivid.art/template
                     :o (s/? (s/keys :opt-un [:vivid.art/bindings
                                              :vivid.art/delimiters
                                              :vivid.art/to-phase]))))
; TODO Change (render) options parameter to accept an trailing list of keyword opts rather than a map.
