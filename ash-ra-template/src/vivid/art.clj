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

(ns vivid.art
  "Ash Ra Template public API."
  (:require
    [clojure.spec.alpha :as s]
    [special.core :refer [condition manage]]
    [vivid.art.delimiters :refer [erb]]
    [vivid.art.evaluate :refer [evaluate]]
    [vivid.art.failure :refer [make-failure]]
    [vivid.art.parse :refer [parse]]
    [vivid.art.specs :refer [render-phases to-phase?]]
    [vivid.art.xlate :refer [translate]]))

(def ^:const art-filename-suffix ".art")
(def ^:const art-filename-suffix-regex #"\.art$")

(def ^:const default-delimiters-name "erb")
(def ^:const default-delimiters (var-get
                                  (ns-resolve 'vivid.art.delimiters
                                              (symbol default-delimiters-name))))

(def ^:const failure? vivid.art.failure/failure?)

(def ^:const default-to-phase (last render-phases))

(defn render
  "Renders an input string containing Ash Ra Template (ART) -formatted content
  to an output string."
  ([^String template] (render template {}))
  ([^String template
    {:keys [bindings delimiters dependencies to-phase]
     :or   {bindings {} delimiters default-delimiters dependencies {} to-phase default-to-phase}}]
   (if template
     (let [render* #(cond-> template
                        (to-phase? :parse     to-phase) (parse delimiters)
                        (to-phase? :translate to-phase) (translate)
                        (to-phase? :evaluate  to-phase) (evaluate bindings dependencies))
           f (manage render*
                     :parse-error #(make-failure :parse-error % template))]
       (f)))))
(s/fdef render
        :args (s/cat :t :vivid.art/template
                     :o (s/? (s/keys :opt-un [:vivid.art/bindings
                                              :vivid.art/delimiters
                                              :vivid.art/dependencies
                                              :vivid.art/to-phase]))))
