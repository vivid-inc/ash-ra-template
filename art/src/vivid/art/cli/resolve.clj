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

(ns vivid.art.cli.resolve
  "Resolvers of CLI option arguments.

  If an argument doesn't appear to be resolvable according to the nature of
  a given resolver fn, it returns nil. Otherwise, these fns proceed with
  resolution and will signal any errors encountered as :vivid.art.cli/error
  conditions, :normally nil for production but special/manage'd for use
  in automated testing.

  As an example, a file resolver function interprets its argument as a file
  path. If the file doesn't exist, the resolver fn sensibly returns nil, but
  if the file does exist, then any errors associated with attempting to read
  the file's contents is signalled."
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.string]
    [special.core :as special])
  (:import
    (java.io File IOException PushbackReader)))

(defn resolve-as-edn-file
  "Attempt to interpret a value as a path to an EDN file. The value would then
  be an io/reader source, either a string filename or an io/resource."
  [x]
  (when (and (string? x)
             (seq (clojure.string/trim x)))
    (let [f (io/file (System/getProperty "user.dir") x)]
      (when (.exists f)
        (try
          (with-open [r (io/reader f)]
            (edn/read (PushbackReader. r)))
          (catch IOException e
            (special/condition :vivid.art.cli/error
                               {:step      'resolve-as-edn-file
                                :message   (str "Error opening file: " x \newline
                                                (.getMessage e) \newline
                                                (.getStackTrace e))
                                :file      f
                                :exception e}
                               :normally nil))
          (catch RuntimeException e
            (special/condition :vivid.art.cli/error
                               {:step      'resolve-as-edn-file
                                :message   (str "Error reading EDN from file: " x \newline
                                                (.getMessage e) \newline
                                                (.getStackTrace e))
                                :file      f
                                :exception e}
                               :normally nil)))))))

(defn resolve-as-edn-literal
  "Attempt to interpret a value as an EDN string."
  [x]
  (when (string? x)
    (try
      (edn/read-string x)
      (catch RuntimeException _
        nil))))

(defn resolve-as-file
  "Attempt to interpret a value as a java.io.File."
  [path]
  (cond
    (instance? File path)                                 path
    (and (string? path) (seq (clojure.string/trim path))) (File. ^String path)
    :else                                                 nil))

(defn resolve-as-map
  "Attempt to interpret a value as a Clojure map."
  [x]
  (when (map? x)
    x))

(defn resolve-as-var
  "Attempt to interpret a value as the name of a namespace-(un)qualified var,
  defaulting to default-ns if specified. The value would then be a string,
  a symbol, or a var."
  [x & [default-ns]]
  (as-> x s
        (if (and (string? s) (seq s))
          (symbol s) s)
        (if (symbol? s)
          (if default-ns
            (ns-resolve default-ns s)
            (resolve s))
          s)
        (if (var? s)
          (var-get s)
          nil)))
