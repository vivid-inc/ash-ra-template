(set-env! :dependencies '[[vivid/boot-art "0.6.0"]])

(require '[clojure.java.io :as io]
         '[vivid.boot-art :refer [art]])

(deftask rndr []
  (art :templates  #{(io/file "source/index.html.art")
                     (io/file "templates")}
       :output-dir (io/file "artifax")))
