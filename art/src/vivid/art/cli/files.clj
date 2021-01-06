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

(ns vivid.art.cli.files
  "File and path handling common to this ART CLI library in general."
  (:require
    [clojure.string]
    [vivid.art :as art])
  (:import
    (java.io File)))

(defn- art-template-file?
  [^File f]
  (and (.isFile f)
       (.endsWith (.getName f) art/art-filename-suffix)))

(defn- path-seq
  "File's path components as a seq."
  [^File path]
  (some->> path
           (.getCanonicalFile)
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
  [f]
  (clojure.string/replace f art/art-filename-suffix-regex ""))

(defn template-file-seq
  "seq of all .art template files within the sub-dir hierarchy rooted in path."
  [^File path]
  (filter art-template-file? (file-seq path)))
