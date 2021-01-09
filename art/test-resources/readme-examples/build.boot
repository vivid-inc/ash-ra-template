(set-env! :dependencies '[[boot/core "2.8.3" :scope "provided"]
                          [vivid/boot-art "0.5.0"]])

(require '[vivid.art.boot-task :refer [art]]
         '[clojure.java.io :as io])
(import '(java.io File))

(deftask render-art []
  (comp 
    (art :bindings   '{mysterious-primes [7 191]}
         :delimiters {:begin-forms "{%" :end-forms "%}" :begin-eval "{%=" :end-eval "%}"}
         :files      ["templates/oracle.art"]
         :output-dir (File. "target"))))
