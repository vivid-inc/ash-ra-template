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

(ns vivid.art.boot-task
  {:b/export-tasks true}
  (:require
    [boot.core :as boot :refer [deftask]]
    [boot.util :as util]
    [vivid.art :as art]
    [vivid.art.cli.usage])
  (:import
    (java.io File)))

; Referencing https://github.com/boot-clj/boot/wiki/Filesets

(defn ^String strip-suffix
  [^String s ^String suffix]
  (subs s 0 (- (count s) (count suffix))))

(defn render-file
  [^File src-file output-dir options]
  (try
    (let [src-name (.getName src-file)
          src-path (.getPath src-file)
          dest-name (strip-suffix src-name art/art-filename-suffix)
          dest-path (clojure.java.io/file output-dir dest-name)]
      (util/info (format "Rendering ART %s" (.getCanonicalPath dest-path)) \newline)
      (clojure.java.io/make-parents dest-path)
      (-> src-path
          (slurp)
          (as-> c (art/render c (select-keys options
                                             [:bindings
                                              :delimiters
                                              :dependencies
                                              :to-phase])))
          (->> (spit dest-path))))
    (catch Exception e
      (util/fail (str "Failed to render ART template " src-file "\n"))
      (util/fail (.getMessage e))
(.printStackTrace e)
      (util/exit-error))))

(defn from-cli-args
  [files boot-fileset output-dir options]
  (let [->file (fn [file] (clojure.java.io/file file))]
    (doseq [f files]
      (render-file (->file f) output-dir options))
    ; Add rendered .art files to Boot's fileset
    (-> boot-fileset
        (boot/add-resource output-dir)
        (boot/commit!))))

(defn from-boot-fileset
  [boot-fileset prev-fileset output-dir options]
  (let [->file (fn [file] (clojure.java.io/file (:dir file) (:path file)))
        art-files (->> boot-fileset
                       (boot/fileset-diff @prev-fileset)
                       (boot/input-files)
                       (boot/by-ext [art/art-filename-suffix]))]
    (reset! prev-fileset boot-fileset)

    ; Render ART files to the designated output-dir
    (doseq [f art-files]
      (render-file (->file f) output-dir options))

    ; Replace .art files with their rendered counterparts
    (-> boot-fileset
        (boot/rm art-files)
        (boot/add-resource output-dir)
        (boot/commit!))))

(boot.core/deftask
  art
  ; TODO How to create this task docstring dynamically?
  "Render Ash Ra .art templates.

Provided one or more template files and any quantity of optional bindings, this
Boot task writes rendered template output to a specified output dir.
Templates are rendered to files whose filenames are stripped of the .art suffix."
  [b bindings VAL code "Bindings made available to templates for symbol resolution"
   d delimiters VAL code "Template delimiters (default: `erb')"
   _ dependencies VAL code "Clojure deps map providing libs within the template evaluation environment"
   p to-phase VAL code "Stop the render dataflow on each template at an earlier phase"
   f files FILES [str] "A vector of .art template files to render. If not present, all files will be rendered"
   o output-dir DIR file "Write rendered files to DIR. Leave unset to have Boot decide"]
  (let [output-dir' (or output-dir (boot/tmp-dir!))
        prev-fileset (atom nil)]
    (boot/with-pre-wrap
      boot-fileset
      (if files
        (from-cli-args files boot-fileset output-dir' *opts*)
        (from-boot-fileset boot-fileset prev-fileset output-dir' *opts*)))))
