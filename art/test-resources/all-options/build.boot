(set-env! :dependencies '[[boot/core "2.8.3" :scope "provided"]
                          [vivid/boot-art "0.5.0"]])

(require '[vivid.boot-art :refer [art]]
         '[clojure.java.io :as io])
(import '(java.io File))

(deftask render-art []
  (comp 
    (art :bindings     '{updated "2021-01-01"}
         :dependencies '{hiccup {:mvn/version "1.0.5"}}
         :delimiters   {:begin-forms "{%" :end-forms "%}" :begin-eval "{%=" :end-eval "%}"}
         :files        [(io/file "templates")]
         :output-dir   (io/file "target")
         :to-phase     :evaluate)))
