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

(defn summary [what-i-am]
  (->> ["Render Ash Ra .art templates."
        (format "Provided file or directory tree paths containing Ash Ra .art template files and an output dir, this
%s renders the ART templates to the output dir, preserving relative sub-paths." what-i-am)]
       (clojure.string/join "\n\n")))

; CLI options are specified according to clojure.tools.cli.
; Entries are sorted alphabetically by long option.
(def ^:const cli-options
  [[;; --bindings is passed through to vivid.art/render
    nil "--bindings PARAM"
    "Bindings made available to templates for symbol resolution"]

   [;; --delimiters is passed through to vivid.art/render
    nil "--delimiters PARAM"
    (format "Template delimiters (default: `%s')" vivid.art/default-delimiters-name)]

   [;; --dependencies is passed through to vivid.art/render
    nil "--dependencies PARAM"
    "Clojure deps map providing libs to the template evaluation environment"]

   ["-h" "--help"
    "Display this lovely help and exit"]

   [;; vivid.art.cli/render-batch writes rendered output under --output-dir
    ;; This is required.
    nil "--output-dir DIR"
    (format "Write rendered files to DIR (default: `%s')" default-output-dir)
    :default default-output-dir]

   [;; --to-phase is passed through to vivid.art/render
    nil "--to-phase PARAM"
    "Stop the render dataflow on each template at an earlier phase"
    ]])

(defn finer-details [invocation-phrase]
  (str
    "CLI arguments can be freely mixed, but are processed in order of appearance which might
be important to you in case of collisions. Depending on what types of values a particular
option accepts and whether ART was invoked from the CLI or " invocation-phrase ",
ART attempts to interpret each value in this order of precedence:
  1. As a Clojure map literal.
  2. As an (un-)qualified var.
  3. As a string path to an EDN file.
  4. As an EDN literal.

Rendered output files are written to output-dir stripped of their .art filename suffixes, overwriting
any existing files with the same names. output-dir and sub-paths therein are created as necessary. "))

(def ^:const for-more-info "For more info, see\n  https://github.com/vivid-inc/ash-ra-template")
