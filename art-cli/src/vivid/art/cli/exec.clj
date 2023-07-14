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

(ns ^:internal-api vivid.art.cli.exec
  "Non-lazily drives the rendering of batches."
  (:require
   [clojure.java.io :as io]
   [clojure.pprint]
   [farolero.core :as farolero]
   [vivid.art :as art]
   [vivid.art.cli.log :as log])
  (:import
   (java.io File)))

(defn render-file
  [{:keys [^File src-path ^File dest-rel-path] :as template-file} {:keys [^File output-dir] :as batch}]
  (try
    (let [output-path ^File (io/file output-dir dest-rel-path)
          to-phase (get batch :to-phase vivid.art/default-to-phase)]
      (log/*info-fn* (format "Rendering ART %s" (.getAbsoluteFile output-path)))
      (io/make-parents output-path)
      (as-> (slurp src-path) c
        (apply art/render c (mapcat identity
                                    (select-keys batch [:bindings
                                                        :delimiters
                                                        :dependencies
                                                        :to-phase])))
        (if (to-phase #{:parse :translate})
          (clojure.pprint/pprint c (io/writer output-path)) ; Possibly more readable
          (spit output-path c))))
    (catch Exception e
      (farolero/signal :vivid.art.cli/error
                       {:step      'render-file
                        :message   (format "Exception while rendering ART template %s"
                                           (.getAbsoluteFile ^File (:src-path template-file)))
                        :exception e}))))
