;; This Boot project demonstrates configuration of multi-batch rendering.
;;
;; Run with:
;;
;;   $ ./test.sh boot rndr

(set-env! :dependencies '[[boot/core "2.8.3" :scope "provided"]
                          [net.vivid-inc/boot-art "0.6.1"]])

(require '[vivid.boot-art :refer [art]]
         '[clojure.java.io :as io])

(deftask rndr []
  (comp
    ; An ART render batch configuration
    (art :templates    [(io/file "src/templates/css")]
         :dependencies '{garden {:mvn/version "1.3.10"}}
         :output-dir   (io/file "src/resources"))

    ; Another, different batch
    (art :templates  [(io/file "src/templates/java")]
         :bindings   '{version "1.2.3"}
         :output-dir (io/file "target/generated-sources/java"))))
