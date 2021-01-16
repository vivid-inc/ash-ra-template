(set-env! :dependencies '[[vivid/boot-art "0.5.0"]]
          :resource-paths #{"templates"})

(require '[clojure.java.io :as io]
         '[vivid.boot-art :refer [art]])

(deftask rndr []
  (comp (art :dependencies {'org.clojure/clojure {:mvn/version "1.10.1"}})
        (target)))
