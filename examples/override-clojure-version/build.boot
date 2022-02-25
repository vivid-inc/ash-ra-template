(set-env! :dependencies '[[net.vivid-inc/boot-art "0.6.1"]]
          :resource-paths #{"templates"})

(require '[clojure.java.io :as io]
         '[vivid.boot-art :refer [art]])

(deftask rndr []
  (comp (art :dependencies '{org.clojure/clojure {:mvn/version "1.10.1"}})
        (target)))
