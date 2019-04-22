; Copyright 2019 Vivid Inc.

; TODO [f art-file FILE str "Input file. If not present, all .art template files will be rendered."]

(ns vivid.art.boot
  {:b/export-tasks true}
  (:require
    [boot.core :as boot :refer [deftask]]
    [boot.util]
    [clojure.java.io]
    ;[vivid.art.core]
    ))

(def ^:const art-file-suffix ".art")

(defn ^String strip-suffix
  [^String s ^String suffix]
  (subs s 0 (- (count s) (count suffix))))

(boot.core/deftask
  art
  "Render Ash Ra .art template files.

  The .art suffix is stripped from the rendered output's filenames."
  []
  (let [last-fileset (atom nil)]
    (boot/with-pre-wrap fileset
                        (let [tmp (boot/tmp-dir!)
                              art-files (->> fileset
                                             (boot/fileset-diff @last-fileset)
                                             (boot/input-files)
                                             (boot/by-ext [art-file-suffix]))]

                          (reset! last-fileset fileset)
                          ;(boot.util/info (with-out-str (clojure.pprint/pprint tmp)) "\n")
                          ;(boot.util/info (with-out-str (clojure.pprint/pprint art-files)) "\n")

                          ; Render templates to a temporary destination
                          (doseq [file art-files]
                            (try
                              (let [src-path (:path file)
                                    src-full-path (clojure.java.io/file (:dir file) (:path file))
                                    dest-path (strip-suffix (:path file) art-file-suffix)
                                    dest-full-path (clojure.java.io/file tmp dest-path)]
                                ;(boot.util/info (with-out-str (clojure.pprint/pprint file)))
                                (boot.util/info "Rendering ART %s\n" src-path)
                                (clojure.java.io/make-parents dest-full-path)
                                (->> src-full-path
                                     (slurp)
                                     (spit dest-full-path)))
                              (catch Exception e
                                (boot.util/fail (str "Failed to render ART template " file))
                                (boot.util/fail (.getMessage e))
                                (boot.util/exit-error))))

                          ; Transformation:
                          ; Read template -> render output -> write to a new temp file.
                          ; Replace .art files with their rendered counterparts in the fileset.
                          (-> fileset
                              (boot/rm art-files)
                              (boot/add-resource tmp)
                              (boot/commit!))))))
