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

(ns vivid.art.cli.files
  "File and path handling common to this ART CLI library in general."
  (:require
   [clojure.java.io :as io]
   [clojure.string]
   [farolero.core :as farolero]
   [vivid.art.cli])
  (:import
   (java.io File)))

(def ^:const prohibited-template-output-filenames
  "Attempting to (over-)write or delete these filenames might have
  undesirable or catastrophic consequences."
  #{"." ".."})

(defn art-template-file?
  [^File f]
  (and (.isFile f)
       (.endsWith (.getName f) vivid.art.cli/art-filename-suffix)))

(defn- path-seq
  "File's path components as a seq."
  [^File path]
  (some->> path
           ; Honor symlinks. By definition, File.getCanonicalFile does not.
           (.getAbsoluteFile)
           (.toPath)
           (.iterator)
           (iterator-seq)
           (map str)))

(defn- remove-common-heads
  "Given two sequences, returns a pair of paths with their mutual common heads
  removed, leaving just their tails starting at the point where they differ."
  [x-seq y-seq]
  (loop [xs x-seq
         ys y-seq]
    (if (and (seq xs) (seq ys)
             (= (first xs) (first ys)))
      (recur (rest xs) (rest ys))
      [xs ys])))

(defn relative-path
  "A seq of path elements (as strings) from base to path."
  [^File base ^File path]
  (let [b (path-seq base)
        p (path-seq path)
        [_ rel-path] (remove-common-heads b p)]
    rel-path))

(defn strip-art-filename-suffix
  [path]
  (let [out (clojure.string/replace path vivid.art.cli/art-filename-suffix-regex "")
        filename (.getName (File. ^String out))]
    (when (get prohibited-template-output-filenames filename)
      (farolero/signal :vivid.art.cli/error
                       {:step    'strip-art-filename-suffix
                        :message (format "Cowardly refusing to create output file named '%s' from path: '%s'" out path)}))
    out))

(defn template-file-seq
  "seq of all .art template files within the sub-dir hierarchy rooted in path."
  [^File path]
  (filter art-template-file? (file-seq path)))

(defn ->template-path
  "Takes a base path and a path to a template-file (ostensibly within the
      base path) and returns a map indicating the providence :src-path and the
      intended output path of the template file :dest-rel-path relative to the
      batch's :output-dir."
  [^File base-path ^File template-file]
  (let [rel-path-parent (relative-path base-path (.getParentFile template-file))
        dest-name (strip-art-filename-suffix (.getName template-file))
        dest-rel-path (apply io/file (concat rel-path-parent
                                             [dest-name]))]
    {:src-path      template-file
     :dest-rel-path dest-rel-path}))

(defn paths->template-paths!
  "Finds all ART templates either at the given paths (as template files) or
      within their sub-trees (as a directory). This function is impure, as it
      directly scans the filesystem subtree of each of the paths."
  [paths]
  (letfn [(->template-paths [base-path]
            (let [template-files (template-file-seq base-path)]
              (map #(->template-path base-path %) template-files)))]
    (mapcat ->template-paths paths)))
