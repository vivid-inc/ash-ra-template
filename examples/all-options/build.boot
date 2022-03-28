(set-env! :dependencies '[[boot/core "2.8.3" :scope "provided"]
                          [net.vivid-inc/boot-art "0.7.0"]])

(require '[vivid.boot-art :refer [art]]
         '[clojure.java.io :as io])
(import '(java.io File))

(deftask rndr []
  (comp 
    (art :bindings     '{updated "2021-01-01"}
         :dependencies '[[hiccup/hiccup "1.0.5" :exclusions [org.clojure/clojure]]]
         :delimiters   {:begin-forms "{%" :end-forms "%}" :begin-eval "{%=" :end-eval "%}"}
         :templates    [(io/file "templates")]
         :output-dir   (io/file "target")
         :to-phase     :evaluate)))
