; Copyright 2024 Vivid Inc. and/or its affiliates.
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

(ns ^:internal-api vivid.art.cli.files
  "File and path handling common to this ART CLI library in general."
  (:require
   [clojure.java.io :as io]
   [clojure.string]
   [farolero.core :as farolero])
  (:import
   (java.io File)
   (java.nio.file Path Paths)))

(set! *warn-on-reflection* true)

(def ^:const art-filename-suffix ".art")
(def ^:const art-filename-suffix-regex #"\.art$")

(def ^:const prohibited-template-output-filenames
  "Attempting to (over-)write or delete these filenames might have
  undesirable or catastrophic consequences."
  #{"." ".."})

(defn art-template-file?
  [^File f]
  (and (.isFile f)
       (.endsWith (.getName f) art-filename-suffix)))

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
  (let [out      (clojure.string/replace path art-filename-suffix-regex "")
        filename (.getName (File. ^String out))]
    (when (get prohibited-template-output-filenames filename)
      (farolero/signal :vivid.art.cli/error
                       {:step    'strip-art-filename-suffix
                        :message (format "Cowardly refusing to create output file named '%s' from path: '%s'" out path)}))
    out))

(defn template-path-metadata
  "Takes a base path and a path to a template-file (ostensibly within the
  base path) and returns a map indicating the providence :src-path and the
  intended output path of the template file :dest-rel-path relative to the
  batch's :output-dir."
  [^File base-path ^File template-file]
  (let [rel-path-parent (relative-path base-path (.getParentFile template-file))
        dest-name       (strip-art-filename-suffix (.getName template-file))
        dest-rel-path   (apply io/file (concat rel-path-parent [dest-name]))]
    {:src-path      template-file
     :dest-rel-path dest-rel-path}))

; Specify sets of ART templates on the filesystem using a path specification.
; path-spec resolution is attempted in the following order:
; - A glob, using Java's PathMatcher
;   See https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-
; - A directory that actually exists on the filesystem, relative to the current working directory.
; - A path to a single file.

(defn ^Path ->path [p & ps] (Paths/get p (into-array String ps)))
(def ^:const glob-special-characters #"(?<!\\)[\*\?\{\[]")
(defn globbed-path-element? [p] (re-find glob-special-characters (.toString (.getFileName ^Path p))))

(defn orient-path-spec
  "Determines, or orients, what a template path specification refers to on the file system. Returns:

  :base-dir, the last directory element before either the first globby path element or just the template file.
  :oriented-as, indicating what the template path spec was interpreted as: a :glob, a :directory, or a :file.
  :pathmatcher-arg, the remainder of path-spec starting with the globby path element or just the template file,
    as a string argument in PathMatcher syntax.

  :base-dir (after conversion to a ^File) and :pathmatcher-arg are meant to be directly passed as arguments
  to java.nio.file.FileSystems::getPathMatcher.
  If the spec doesn't contain any special characters and the last path element is not a file (doesn't exist on the
  filesystem as a file), a glob of art files assuming the default art file suffix will be implicitly added."
  [^String path-spec]
  (cond
    ; Does path-spec use any globbing characters?
    (re-find glob-special-characters path-spec)
    ; Situate the base directory of the glob to just before where it starts.
    (let [p (->path "." path-spec)]
      ; Implementation note: Path::subpath is stubbornly idiosyncratic in its treatment of its index args.
      ; The code contorts itself to provide values that elicit the desired response.
      (loop [idx 0]
        (cond
          ; Is this path element a glob?
          ; A glob cannot occur at idx 0, whose path element value is defined as "." above.
          (globbed-path-element? (.getName p idx))
          (let [base-dir (if (>= 1 idx) (->path ".")
                                        (.subpath p 1 idx))]
            {:base-dir        (.toFile base-dir)
             :oriented-as     :glob
             :path-spec       path-spec
             :pathmatcher-arg (str "glob:" path-spec)})

          :else
          (recur (inc idx)))))

    ; A directory, extant in the filesystem?
    (.isDirectory (.toFile (->path path-spec)))
    ; path-spec refers to an extant directory; implies all subordinate ART files.
    {:base-dir        (File. path-spec)
     :oriented-as     :directory
     :path-spec       path-spec
     :pathmatcher-arg (str "glob:" (->path path-spec (str "**" art-filename-suffix)))}

    ; Not a glob and not an extant directory; the last option is to assume it's a file.
    :else
    {:base-dir        (File. ".")
     :oriented-as     :file
     :path-spec       path-spec
     :pathmatcher-arg (str "glob:" path-spec)}))
