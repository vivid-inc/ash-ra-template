; Copyright 2023 Vivid Inc. and/or its affiliates.
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

(ns ^:internal-api vivid.art.cli.specs
  (:require
   [clojure.spec.alpha :as s])
  (:import
   (java.io File)))

; Additional classpath, configured for the template rendering environment.
(s/def ::classpath (s/coll-of string? :min-count 1))

; art-cli accepts a dependencies value using Leiningen's format.
(s/def ::lein-dependency (s/cat :lib symbol? :version string? :more (s/* any?)))
(s/def ::lein-dependencies (s/coll-of ::lein-dependency))

; art-cli template files.
(s/def ::file (partial instance? File))
(s/def :vivid.art.cli/template-file (s/keys :req-un [::src-path
                                                     ::dest-rel-path]))

; Specification of a rendering batch.
(s/def :vivid.art.cli/classpath ::classpath)
(s/def :vivid.art.cli/dependencies ::lein-dependencies)
(s/def :vivid.art.cli/output-dir ::file)
(s/def :vivid.art.cli/templates (s/coll-of ::file :min-count 1))

; A render batch, describing the input & output files and all
; (vivid.art/render) options.
(s/def :vivid.art.cli/batch (s/keys :req-un [:vivid.art.cli/output-dir
                                             :vivid.art.cli/templates]
                                    :opt-un [:vivid.art.cli/classpath
                                             :vivid.art.cli/dependencies
                                             :vivid.art/bindings
                                             :vivid.art/delimiters
                                             :vivid.art/to-phase]))
