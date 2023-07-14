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

(ns vivid.art.cli.args
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string]
   [clojure.tools.cli]
   [farolero.core :as farolero]
   [vivid.art.cli.specs]
   [vivid.art.cli.validate :as validate]))

(defn- parse-cli-args
  [args options-spec]
  (let [{:keys [options arguments errors]} (clojure.tools.cli/parse-opts args options-spec)]
    (cond
      (:help options)
      (farolero/signal :vivid.art.cli/error
                       {:step        'parse-cli-args
                        :exit-status 0
                        :show-usage  true})

      errors
      (farolero/signal :vivid.art.cli/error
                       {:step    'parse-cli-args
                        :message (clojure.string/join \newline errors)})

      (= 0 (count arguments))
      (farolero/signal :vivid.art.cli/error
                       {:step       'parse-cli-args
                        :show-usage true})

      :else
      [arguments options])))

(defn validate-as-batch
  "Validates template paths, and resolves and validates everything else,
  returning a render batch."
  [templates {:keys [^String output-dir] :as options}]
  (merge
    ; Mandatory
   {:templates (validate/validate-templates templates)
    :output-dir (validate/validate-output-dir output-dir)}
    ; Optional
   (when-let [bindings (when (seq (:bindings options)) (:bindings options))]
     {:bindings (validate/validate-bindings bindings)})
   (when-let [delimiters (:delimiters options)]
     {:delimiters (validate/validate-delimiters delimiters)})
   (when-let [dependencies (:dependencies options)]
     {:dependencies (validate/validate-dependencies dependencies)})
   (when-let [to-phase (:to-phase options)]
     {:to-phase (validate/validate-to-phase to-phase)})))

(defn direct->batch
  [templates options]
  (validate-as-batch templates options))

(defn cli-args->batch
  "Interpret command line arguments, producing a map representing an ART render
  batch job that can be executed by this CLI lib's (render-batch) fn. All
  arguments are validated according to the clojure.tools.cli -compliant options
  specification and resolved. Exceptional cases are signaled."
  [args options-spec]
  (let [[arguments options] (parse-cli-args args options-spec)]
    (validate-as-batch arguments options)))

(s/fdef direct->batch
  :ret :vivid.art.cli/batch)
(s/fdef cli-args->batch
  :ret :vivid.art.cli/batch)
