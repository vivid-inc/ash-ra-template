(set-env! :dependencies '[[boot/core "2.8.3" :scope "provided"]
                          [net.vivid-inc/boot-art "0.7.0"]])

(require '[vivid.boot-art :refer [art]]
         '[clojure.java.io :as io])

(deftask rndr []
  (comp
    (art :templates  [(io/file "templates")]
         :output-dir (io/file "target"))))
