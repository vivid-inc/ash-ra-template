;; Run with:
;;
;;   $ lein art

(defproject art-example--multiple-batches "0"

  ; Add the lein-art Leiningen plugin:
  :plugins [[vivid/lein-art "0.5.0"]]

  ; Render .art templates
  :art [; An ART render batch configuration
        {:templates    "src/templates/css"
         :output-dir   "src/resources"}

        ; Another, different batch
        {:templates  ["src/templates/java"]
         :output-dir "target/generated-sources/java"})
