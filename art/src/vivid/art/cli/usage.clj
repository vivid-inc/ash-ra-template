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

(ns vivid.art.cli.usage
  (:require
    [clojure.string]
    [vivid.art]
    [vivid.art.specs]))

(def ^:const default-output-dir ".")

(def ^:const one-line-desc "Render Ash Ra .art templates.")

(defn summary [what-i-am]
  (str "Provided file or directory tree paths containing Ash Ra .art template files, this
" what-i-am " renders the ART templates to the output dir, preserving relative sub-paths."))

; CLI options are specified according to clojure.tools.cli.
; Entries are sorted alphabetically by long option.
(def ^:const cli-options
  [[;; --bindings is passed through to vivid.art/render
    nil "--bindings VAL"
    "Bindings made available to templates for symbol resolution"
    ; TODO Enable :multi -ple --bindings when org.clojure/tools.cli is updated.
    ;:multi true :default [] :update-fn conj
    ]

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
    "Stop the render dataflow on each template at an earlier phase"
    ]])

(def ^:const for-more-info "For more info, see\n  https://github.com/vivid-inc/ash-ra-template")
