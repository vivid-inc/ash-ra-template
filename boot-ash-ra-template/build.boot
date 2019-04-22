; For the sake of IntelliJ & Cursive
(require '[boot.core :refer [deftask set-env! task-options!]]
         '[boot.task.built-in :refer [install jar pom]])

(def project 'vivid/boot-ash-ra-template)
(def version "0.1.0")

(set-env! :resource-paths #{"src"}
          :source-paths #{"src"}
          :dependencies '[[org.clojure/clojure "1.9.0" :scope "provided"]
                          [boot/core "2.8.3" :scope "provided"]
                          [onetom/boot-lein-generate "0.1.3" :scope "test"]
                          [vivid/ash-ra-template "0.2.0"]])

(require '[vivid.art.boot :refer [art]])

(task-options!
  pom {:project     project
       :version     version
       :description "Boot task for processing Ash Ra ART templates."
       :url         "https://github.com/vivid/ash-ra-template"
       :scm         {:url "https://github.com/vivid/ash-ra-template"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}})

; Generate a Leiningen project.clj file for the sake of IntelliJ & Cursive
(require '[boot.lein])
(boot.lein/generate)

(deftask build
         "Build and install the project locally."
         []
         (comp (pom) (jar) (install)))
