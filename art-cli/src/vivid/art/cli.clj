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

(ns vivid.art.cli
  "art-cli public API."
  (:require
   [clojure.spec.alpha :as s]
   [vivid.art.cli.classpath :refer [with-custom-classloader]]
   [vivid.art.cli.exec]
   [vivid.art.cli.files]
   [vivid.art.cli.log :as log]
   [vivid.art.cli.specs]))

(def ^:const art-filename-suffix
  "Ash Ra Template .art filename suffix."
  vivid.art.cli.files/art-filename-suffix)
(def ^:const art-filename-suffix-regex
  "Ash Ra Template .art filename suffix as a regular expression, suitable for matching filenames."
  vivid.art.cli.files/art-filename-suffix-regex)

(defn render-batch
  "Scans :templates for files and directory sub-trees, renders all ART templates found
  within according to the batch settings. Fails fast in event of an error."
  [batch]
  (let [templates (-> (:templates batch)
                      vivid.art.cli.files/paths->template-paths!)]
    (if (empty? templates)
      (log/*warn-fn* "Warning: No ART templates to render.")
      (let [classpath (vivid.art.cli.classpath/assemble-classpath batch)]
        (with-custom-classloader classpath
                                 (doseq [template-file templates]
                                   (vivid.art.cli.exec/render-file template-file batch)))))))
(s/fdef render-batch
        :args (s/cat :batch (s/? :vivid.art.cli/batch)))

(defn render-batches
  "Render a collection of batches."
  [batch-coll]
  (doseq [b batch-coll]
    (render-batch b)))
(s/fdef render-batches
  :args (s/coll-of :vivid.art.cli/batch))
