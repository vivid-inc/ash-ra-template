; Copyright 2021 Vivid Inc.
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

(ns vivid.art.cli.specs
  (:require
    [clojure.spec.alpha :as s])
  (:import
    (java.io File)))

(s/def ::file (partial instance? File))
(s/def :vivid.art.cli/template-file (s/keys :req-un [::src-path
                                                     ::dest-rel-path]))
(s/def :vivid.art.cli/templates (s/or :single-file :vivid.art.cli/template-file
                                      :coll-file   (s/coll-of :vivid.art.cli/template-file :min-count 1)))
(s/def :vivid.art.cli/output-dir ::file)

; A render batch, describing the input & output files and all
; (vivid.art/render) options.
(s/def :vivid.art.cli/batch (s/keys :req-un [:vivid.art.cli/templates
                                             :vivid.art.cli/output-dir]
                                    :opt-un [:vivid.art/bindings
                                             :vivid.art/delimiters
                                             :vivid.art/dependencies
                                             :vivid.art/to-phase]))
