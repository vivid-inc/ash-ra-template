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

(ns ^:internal-api vivid.art.cli.usage
  (:require
   [clojure.string]
   [vivid.art]
   [vivid.art.specs]))

(def ^:const default-output-dir ".")

(def ^:const one-line-desc "Render Ash Ra .art templates.")

(defn summary [what-i-am]
  (str "Provided file or directory tree paths containing Ash Ra .art template files, this
" what-i-am " renders the ART templates to the output dir, preserving relative sub-paths."))

(def ^:const cli-commands
  [{:command     "config"
    :description "Dump the effective configuration and exit"}
   {:command     "help"
    :description "Display this lovely help and exit"}
   {:command     "render"
    :description "Render all template batches once"}
   {:command     "watch"
    :description "Watch templates in all batches, re-rendering on changes"}])

(defn cli-command? [s]
  (some #{s} (map :command cli-commands)))

(defn command-summary []
  (->> (map #(format "  %-6s  %s" (:command %) (:description %)) cli-commands)
       (clojure.string/join "\n")))

; CLI options are specified according to clojure.tools.cli.
; Entries are sorted alphabetically by long option.
(def ^:const cli-options
  [[;; --bindings is passed through to vivid.art/render
    nil "--bindings VAL"
    "Bindings made available to templates for symbol resolution"
    ; The data structure must preserve order of appearance as later bindings override earlier ones.
    :update-fn conj :default [] :multi true]

   [;; --delimiters is passed through to vivid.art/render
    nil "--delimiters VAL"
    (format "Template delimiters (default: `%s')" vivid.art/default-delimiters-name)]

   [;; --dependencies is passed through to vivid.art/render
    nil "--dependencies VAL"
    "Clojure deps map providing libs to the template evaluation environment"]

   ["-h" "--help"
    "Display this lovely help and exit"]

   [;; vivid.art.cli/render-batch writes rendered output under --output-dir
    ;; This is required.
    nil "--output-dir DIR"
    (format "Write rendered files to DIR (default: `%s')" default-output-dir)
    :default default-output-dir]

   [;; --to-phase is passed through to vivid.art/render
    nil "--to-phase VAL"
    "Stop the render dataflow on each template at an earlier phase"]])

(def ^:const for-more-info "For more info, see\n  https://github.com/vivid-inc/ash-ra-template")
