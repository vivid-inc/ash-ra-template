; Copyright 2020 Vivid Inc.
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

(ns vivid.art.cli.exec
  "Non-lazily drives the rendering of a batch."
  (:require
    [clojure.java.io :as io]
    [clojure.pprint]
    [clojure.spec.alpha :as s]
    [clojure.string]
    [farolero.core :as farolero]
    [vivid.art :as art]
    [vivid.art.cli.files]
    [vivid.art.cli.classpath :refer [with-custom-classloader]]
    [vivid.art.cli.log :as log]
    [vivid.art.cli.specs])
  (:import
    (java.io File)))

(defn- render-file
  [{:keys [^File src-path ^File dest-rel-path] :as template-file} {:keys [^File output-dir] :as batch}]
  (try
    (let [output-path (io/file output-dir dest-rel-path)
          to-phase (get batch :to-phase vivid.art/default-to-phase)]
      (log/*info-fn* (format "Rendering ART %s" output-path))
      (io/make-parents output-path)
      (as-> (slurp src-path) c
            (art/render c (select-keys batch [:bindings
                                              :delimiters
                                              :dependencies
                                              :to-phase]))
            (if (to-phase #{:parse :translate})
              (clojure.pprint/pprint c (io/writer output-path)) ; Possibly more readable
              (spit output-path c))))
    (catch Exception e
      (farolero/signal :vivid.art.cli/error
                       {:step      'render-file
                        :message   (format "Exception while rendering ART template %s\n%s\n%s"
                                           (.getCanonicalPath ^File (:src-path template-file))
                                           (.toString e)
                                           (clojure.string/join \newline (.getStackTrace e)))
                        :exception e}))))

(defn assemble-classpath
      [batch]
      ; TODO Derive repositories from the calling project as well, provide a default set (Maven Central + Clojars)
      ; TODO Documentation: Ensure you don't add differing versions of the same library.
      (concat []
              (:classpath batch)
              (vivid.art.cli.classpath/dependencies->file-paths (:dependencies batch))))

(defn render-batch
  "Scans :templates for files and directories, renders all ART templates found
  within according to the batch settings. Fails fast in event of an error."
  [{:keys [templates] :as batch}]
  (if (empty? templates)
    (log/*warn-fn* "Warning: No ART templates to render.")
    (let [classpath (assemble-classpath batch)]
         (with-custom-classloader classpath
                                  (doseq [template-file templates]
                                         (render-file template-file batch))))))

(s/fdef render-batch
        :args (s/cat :batch (s/? :vivid.art.cli/batch)))
