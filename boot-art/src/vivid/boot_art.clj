; Copyright 2019 Vivid Inc.

(ns vivid.boot-art
  {:b/export-tasks true}
  (:require
    [boot.core :as boot :refer [deftask]]
    [boot.util :as util]
    [vivid.art :as art])
  (:import (java.io File)))

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
      (util/info "Rendering ART %s\n" src-path)
      (clojure.java.io/make-parents dest-path)
      (-> src-path
          (slurp)
          (as-> c (art/render c (select-keys options
                                             [:bindings
                                              :delimiters
                                              :dependencies])))
          (->> (spit dest-path))))
    (catch Exception e
      (util/fail (str "Failed to render ART template " src-file "\n"))
      (util/fail (.getMessage e))
      (util/exit-error))))

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

(defn from-cli-args-files
  [files boot-fileset output-dir options]
  (let [->file (fn [file] (clojure.java.io/file file))]
    (doseq [f files]
      (render-file (->file f) output-dir options))
    ; Add rendered .art files to Boot's fileset
    (-> boot-fileset
        (boot/add-resource output-dir)
        (boot/commit!))))

(boot.core/deftask
  art
  "Render Ash Ra .art templates.

  Templates are rendered to files whose filenames are stripped of the .art suffix."
  [b bindings VAL code "Bindings made available to templates for symbol resolution."
   d delimiters VAL code "Template delimiters (EDN or a Var)."
   _ dependencies VAL code "Clojure deps map (EDN or a Var)."
   f files FILES [str] "A vector of .art template files to render. If not present, all files will be rendered."]
  (let [output-dir (boot/tmp-dir!)
        prev-fileset (atom nil)]
    (boot/with-pre-wrap
      boot-fileset
      (if files
        (from-cli-args-files files boot-fileset output-dir *opts*)
        (from-boot-fileset boot-fileset prev-fileset output-dir *opts*)))))
