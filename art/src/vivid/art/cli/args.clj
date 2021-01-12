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

(ns vivid.art.cli.args
  (:require
    [clojure.java.io :as io]
    [clojure.spec.alpha :as s]
    [clojure.string]
    [clojure.tools.cli]
    [special.core :as special]
    [vivid.art.cli.files :as files]
    [vivid.art.cli.specs]
    [vivid.art.cli.validate :as validate])
  (:import
    (java.io File)))

(defn- parse-cli-args
  [args options-spec]
  (let [{:keys [options arguments errors]} (clojure.tools.cli/parse-opts args options-spec)]
    (cond
      (:help options)
      (special/condition :vivid.art.cli/error
                         {:step        'parse-cli-args
                          :exit-status 0
                          :show-usage  true})

      errors
      (special/condition :vivid.art.cli/error
                         {:step    'parse-cli-args
                          :message (clojure.string/join \newline errors)})

      (= 0 (count arguments))
      (special/condition :vivid.art.cli/error
                         {:step        'parse-cli-args
                          :show-usage  true})

      :else
      [arguments options])))

(defn ->template-path
  "Takes a base path and a path to a template-file (ostensibly within the
  base path) and returns a map indicating the providence :src-path and the
  intended output path of the template file :dest-rel-path relative to the
  batch's :output-dir."
  [^File base-path ^File template-file]
  (let [rel-path-parent (files/relative-path base-path (.getParentFile template-file))
        dest-name (files/strip-art-filename-suffix (.getName template-file))
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
            (let [template-files (files/template-file-seq base-path)]
              (map #(->template-path base-path %) template-files)))]
    (mapcat ->template-paths paths)))

(defn validate-as-batch
  "Validates template paths, and resolves and validates everything else,
  returning a render batch."
  [arguments {:keys [^String output-dir] :as options}]
  (merge
    ; Mandatory
    {:templates (validate/validate-templates arguments)
     :output-dir (validate/validate-output-dir output-dir)}
    ; Optional
    (when-let [bindings (:bindings options)]
      {:bindings (validate/validate-bindings bindings)})
    (when-let [delimiters (:delimiters options)]
      {:delimiters (validate/validate-delimiters delimiters)})
    (when-let [dependencies (:dependencies options)]
      {:dependencies (validate/validate-dependencies dependencies)})
    (when-let [to-phase (:to-phase options)]
      {:to-phase (validate/validate-to-phase to-phase)})))

(defn cli-args->batch
  "Interpret command line arguments, producing a map representing an ART render
  batch job that can be executed by this CLI lib's (render-batch) fn. All
  arguments are validated according to the clojure.tools.cli -compliant options
  specification and resolved. Exceptional cases are special/condition'ed."
  [args options-spec]
  (let [[arguments options] (parse-cli-args args options-spec)]
    (-> (validate-as-batch arguments options)
        (update :templates paths->template-paths!))))

(s/fdef cli-args->batch
        :ret :vivid.art.cli/batch)
