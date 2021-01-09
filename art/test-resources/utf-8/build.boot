(set-env! :dependencies '[[boot/core "2.8.3" :scope "provided"]
                          [vivid/boot-art "0.5.0"]])

(require '[vivid.art.boot-task :refer [art]]
         '[clojure.java.io :as io])
(import '(java.io File))

(defn files-under-dir [dir]
  (->> (io/file dir)
       (file-seq)
       (filter #(.isFile %))
       (map #(.getPath %))
       (sort)))

(deftask render-art []
  (comp 
    (art :files      (into [] (files-under-dir "templates"))
         :output-dir (File. "target")
         :bindings   (read-string (slurp "greek.edn"))
         :delimiters vivid.art.delimiters/jinja)))
