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
    [vivid.art :as art]
    [vivid.art.cli.args]
    [vivid.art.cli.exec]
    [vivid.art.cli.log :as log]
    [vivid.art.cli.usage]))

; Referencing https://github.com/boot-clj/boot/wiki/Filesets

(defn- exit [_ message]
  (util/fail message)
  ; As of Boot 2.8.3, util/exit-error is hard-coded to return exit-status of 1.
  (util/exit-error))

(defn from-boot-fileset
  [boot-fileset prev-fileset]
  (let [art-files (->> boot-fileset
                       (boot/fileset-diff @prev-fileset)
                       (boot/input-files)
                       (boot/by-ext [art/art-filename-suffix]))]
    (reset! prev-fileset boot-fileset)
    ; .art files will be replaced by their rendered counterparts
    (boot/rm boot-fileset art-files)
    art-files))

(defn- process [options*]
  (binding [log/*info-fn* util/info
            log/*warn-fn* util/warn]
    (boot/with-pre-wrap
      boot-fileset
      (let [options (merge
                      options*
                      (when-not (:output-dir options*)
                        {:output-dir (boot/tmp-dir!)}))
            prev-fileset (atom nil)
            templates (or (:files options*) (from-boot-fileset boot-fileset prev-fileset))]
        (-> (vivid.art.cli.args/validate-as-batch templates options)
            (vivid.art.cli.exec/render-batch))
        (when-not (:output-dir options*)
          (-> boot-fileset
              (boot/add-resource (:output-dir options))
              (boot/commit!)))))))

(boot.core/deftask
  art
  ; TODO How to create this task docstring dynamically?
  "Render Ash Ra .art templates.

Provided one or more template files and any quantity of optional bindings, this
Boot task writes rendered template output to a specified output dir.
Templates are rendered to files whose filenames are stripped of the .art suffix."
  [_ bindings     VAL   ^:! code   "Bindings made available to templates for symbol resolution"
   _ delimiters   VAL   ^:! code   "Template delimiters (default: `erb')"
   _ dependencies VAL   ^:! code   "Clojure deps map providing libs within the template evaluation environment"
   _ files        FILES ^:! [file] "Render these ART files and directory trees thereof, instead of Boot's fileset"
   _ output-dir   DIR   ^:! file   "Divert rendered file output to DIR"
   _ to-phase     VAL   ^:! kw     "Stop the render dataflow on each template at an earlier phase"]
  (process *opts*))
