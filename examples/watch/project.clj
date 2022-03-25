;; Run with:
;;
;;   $ lein auto art
;; or the alias
;;   $ lein watch
;;
;; Modify resources/template.txt.art and when you save it, expect:
;;
;;   auto> Files changed: resources/template.txt.art
;;   auto> Running: lein art
;;   Rendering ART template.txt
;;
;; Note: The resources/ directory is named so allude to the common practice of
;; organizing project resources of varying file types into one directory.
;; The templates/ softlink points to resources/ for the sake of the
;; automated lein-art tests.

(defproject art-example--watch "0"

  :aliases {"watch" ["auto" "art"]}

  ; ART template batch configuration
  :art {:templates    "resources"
        :output-dir   "target"}

  ; lein-auto configuration
  :auto {:default {:file-pattern #"\.(art)$"    ; Monitor .art files for changes ..
                   :paths ["resources"]}}       ; .. in the resources/ directory

  :plugins [[net.vivid-inc/lein-art "0.7.0"]    ; Render ART templates with lein-art
            [lein-auto "0.1.3"]])       ; Monitor files for changes, run a command on change
