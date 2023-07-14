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

(ns vivid.art.cli.resolve
  "Resolvers of CLI option arguments.

  If an argument doesn't appear to be resolvable according to the nature of
  a given resolver fn, it returns nil. Otherwise, these fns proceed with
  resolution and will signal any errors encountered as :vivid.art.cli/error
  conditions, :normally nil for production but managed by the condition
  system for use in automated testing.

  As an example, a file resolver function interprets its argument as a file
  path. If the file doesn't exist, the resolver fn sensibly returns nil, but
  if the file does exist, then any errors associated with attempting to read
  the file's contents is signalled."
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.data.json :as json]
   [clojure.string]
   [farolero.core :as farolero])
  (:import
   (java.io File IOException PushbackReader Reader)))

(defn resolve-as-edn-file
  "Attempt to interpret a value as a path to an EDN file.
  If successful, returns either the file content as a data structure, or when
  option wrap-in-map is true, a map containing a single entry:
  [the filename sans .edn extension as a symbol, the EDN file's contents]."
  [x & options]
  (when (and (string? x)
             (clojure.string/ends-with? x ".edn"))
    (let [{:keys [wrap-in-map] :or {wrap-in-map false}} options
          f ^File (io/file (System/getProperty "user.dir") x)]
      (when (.exists f)
        (try
          (with-open [r ^Reader (io/reader f)]
            (let [content (edn/read (PushbackReader. r))]
              (if wrap-in-map
                (let [n   (clojure.string/replace (.getName f) #"\.edn$" "")
                      sym (symbol n)]
                  {sym (with-meta content {:quote-value? true})})
                content)))
          (catch IOException e
            (farolero/signal :vivid.art.cli/error
                             {:step      'resolve-as-edn-file
                              :message   (str "Error opening file: " x \newline
                                              (.getMessage e) \newline
                                              (.getStackTrace e))
                              :file      f
                              :exception e}
                             :normally nil))
          (catch RuntimeException e
            (farolero/signal :vivid.art.cli/error
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

(defn ^File resolve-as-file
  "Attempt to interpret a value as a java.io.File."
  [path]
  (cond
    (instance? File path)                                 path
    (and (string? path) (seq (clojure.string/trim path))) (File. ^String path)
    :else                                                 nil))

(defn resolve-as-json-file
  "Attempt to interpret a value as a string path to a JSON file.
  If successful, returns either the file content as a data structure, or when
  option wrap-in-map is true, a map containing a single entry:
  [the filename sans .json extension as a symbol, the JSON file's contents]."
  ; TODO This fn is nearly a copy & paste of resolve-as-edn-file
  ; TODO glob -> select only .json files. (.getPathMatcher (java.nio.file.FileSystems/getDefault) "glob:**/*.json") https://clojuredocs.org/clojure.core/file-seq
  [x & options]
  (when (and (string? x)
             (clojure.string/ends-with? x ".json"))
    (let [{:keys [wrap-in-map] :or {wrap-in-map false}} options
          f ^File (io/file (System/getProperty "user.dir") x)]
      (when (.exists f)
        (try
          (with-open [r ^Reader (io/reader f)]
            (let [content (json/read r :eof-error? false)]
              (if wrap-in-map
                (let [n   (clojure.string/replace (.getName f) #"\.json$" "")
                      sym (symbol n)]
                  {sym content})
                content)))
          (catch IOException e
            (farolero/signal :vivid.art.cli/error
                             {:step      'resolve-as-json-file
                              :message   (str "Error opening file: " x \newline
                                              (.getMessage e) \newline
                                              (.getStackTrace e))
                              :file      f
                              :exception e}
                             :normally nil))
          (catch RuntimeException e
            (farolero/signal :vivid.art.cli/error
                             {:step      'resolve-as-json-file
                              :message   (str "Error reading JSON from file: " x \newline
                                              (.getMessage e) \newline
                                              (.getStackTrace e))
                              :file      f
                              :exception e}
                             :normally nil)))))))

(defn resolve-as-list-like
  "Attempt to interpret a value as a Clojure list or vector."
  [x]
  (when (or (list? x) (vector? x))
    x))

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
