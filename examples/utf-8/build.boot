(set-env! :dependencies '[[boot/core "2.8.3" :scope "provided"]
                          [vivid/boot-art "0.6.0"]])

(require '[vivid.art.delimiters]
         '[vivid.boot-art :refer [art]]
         '[clojure.java.io :as io])

(deftask rndr []
  (comp
    (art :templates  [(io/file "templates")]
         :output-dir (io/file "target")
         :bindings   (read-string (slurp "greek.edn"))
         :delimiters vivid.art.delimiters/jinja)))
