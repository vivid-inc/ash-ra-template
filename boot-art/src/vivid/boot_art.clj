; Copyright 2021 Vivid Inc.
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

(ns vivid.boot-art
  {:boot/export-tasks true}
  (:require
    [boot.core :as boot :refer [deftask]]
    [boot.util :as util]
    [clojure.java.io :as io]
    [vivid.art :as art]
    [vivid.art.cli.args]
    [vivid.art.cli.exec]
    [vivid.art.cli.log :as log]
    [vivid.art.cli.usage]))

; Referencing https://github.com/boot-clj/boot/wiki/Filesets

(defn- exit [_ message]
  (util/fail (str message \newline))
  ; As of Boot 2.8.3, util/exit-error is hard-coded to return exit-status of 1.
  (util/exit-error))

(defn boot-file->template-path
  [boot-file]
  {:src-path (io/file (:dir boot-file) (:path boot-file))
   :dest-rel-path (io/file (clojure.string/replace (:path boot-file)
                                                   art/art-filename-suffix-regex ""))})

(defn boot-fileset->template-paths
  [boot-fileset prev-fileset]
  (let [art-files (->> boot-fileset
                       (boot/fileset-diff @prev-fileset)
                       (boot/input-files)
                       (boot/by-ext [art/art-filename-suffix]))
        template-paths (map boot-file->template-path art-files)]
    (reset! prev-fileset boot-fileset)
    [art-files template-paths]))

(defn- process [options*]
  (boot/with-pre-wrap
    boot-fileset
    (binding [log/*info-fn* #(util/info (str % \newline))
              log/*warn-fn* #(util/warn (str % \newline))]
      (let [options (merge
                      options*
                      (when-not (:output-dir options*)
                        {:output-dir (boot/tmp-dir!)}))
            prev-fileset (atom nil)
            batch* (vivid.art.cli.args/validate-as-batch (:templates options*) options)
            [art-files templates] (if (:templates options*)
                                    [[] (vivid.art.cli.args/paths->template-paths! (:templates options*))]
                                    (boot-fileset->template-paths boot-fileset prev-fileset))
            batch (merge batch* {:templates templates})]
        (vivid.art.cli.exec/render-batch batch)
        (cond-> boot-fileset
                ; .art files will be replaced by their rendered counterparts
                (not (:templates options*)) (boot/rm art-files)
                :always (boot/add-resource (:output-dir options))
                :always (boot/commit!))))))

(boot.core/deftask
  art
  ; TODO How to create this task docstring dynamically?
  "Render Ash Ra .art templates.

Provided file or directory tree paths containing Ash Ra .art template files, this
Boot task renders the ART templates to the output dir, preserving relative sub-paths.

For more info, see
 https://github.com/vivid-inc/ash-ra-template"
  [_ bindings     VAL   ^:! code   "Bindings made available to templates for symbol resolution"
   _ delimiters   VAL   ^:! code   "Template delimiters"
   _ dependencies VAL   ^:! code   "Clojure deps map providing libs within the template evaluation environment"
   _ output-dir   DIR   ^:! file   "Divert rendered file output to DIR"
   _ templates    FILES ^:! [file] "Render these ART files and directory trees thereof, instead of Boot's fileset"
   _ to-phase     VAL   ^:! kw     "Stop the render dataflow on each template at an earlier phase"]
  (process *opts*))                                         ; TODO special/manage
