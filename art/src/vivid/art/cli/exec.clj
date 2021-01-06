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
    [special.core :as special]
    [vivid.art :as art]
    [vivid.art.cli.files]
    [vivid.art.cli.log :as log]
    [vivid.art.specs :refer [to-phase?]])
  (:import
    (java.io File)))

(def ^:const cwd (System/getProperty "user.dir"))

(defn template-output-path
  [^File base ^File path output-dir]
  (let [rel-path-parent (vivid.art.cli.files/relative-path base (.getParentFile path))
        dest-name (vivid.art.cli.files/strip-art-filename-suffix (.getName path))
        dest-path (apply io/file (concat [output-dir]
                                         rel-path-parent
                                         [dest-name]))]
    dest-path))

(defn- render-file
  [^File templates-base ^File template-file {:keys [output-dir] :as batch}]
  (try
    (let [output-file (template-output-path templates-base template-file output-dir)
          to-phase (get batch :to-phase vivid.art/default-to-phase)]
      (log/*info-fn* (format "Rendering ART %s" output-file))
      (let [output-path (io/file cwd output-file)]
        (io/make-parents output-path)
        (as-> (slurp template-file) c
              (art/render c (select-keys batch [:bindings
                                                :delimiters
                                                :dependencies
                                                :to-phase]))
              (if (to-phase #{:parse :translate})
                (clojure.pprint/pprint c (clojure.java.io/writer output-path)) ; Possibly more readable
                (spit output-path c)))))
    (catch Exception e
      (special/condition :vivid.art.cli/error
                         {:step      'render-file
                          :message   (format "Exception while rendering ART template %s\n%s\n%s"
                                             template-file
                                             (.toString e)
                                             (clojure.string/join \newline (.getStackTrace e)))
                          :exception e}))))

(defn- render-templates-base
  [t batch]
  (let [templates-base (io/file cwd t)
        template-files (vivid.art.cli.files/template-file-seq templates-base)]
    (doseq [template-file template-files]
      (render-file templates-base template-file batch))))

(defn render-batch
  "Scans :templates for files and directories, renders all ART templates found
  within according to the batch settings. Fails fast in event of an error."
  [{:keys [templates] :as batch}]
  (if (empty? templates)
    (log/*warn-fn* "Warning: No ART templates to render.")
    (doseq [t templates]
      (render-templates-base t batch))))

(s/def ::file (partial instance? File))
(s/def :vivid.art.cli/templates (s/or :single-file ::file
                                      :coll-file   (s/coll-of ::file :min-count 1)))
(s/def :vivid.art.cli/output-dir ::file)
(s/fdef render-batch
        :args (s/cat :batch (s/? (s/keys :req-un [:vivid.art.cli/templates
                                                  :vivid.art.cli/output-dir]
                                         :opt-un [:vivid.art/bindings
                                                  :vivid.art/delimiters
                                                  :vivid.art/dependencies
                                                  :vivid.art/to-phase]))))
