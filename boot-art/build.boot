; Copyright 2019 Vivid Inc.

; For the sake of IntelliJ & Cursive
(require '[boot.core :refer [deftask set-env! task-options!]]
         '[boot.task.built-in :refer [install jar pom]])

(def project 'vivid/boot-art)
(def version "0.4.0")

(set-env! :source-paths #{"test"}
          :resource-paths #{"src"}
          :dependencies '[[org.clojure/clojure "1.9.0" :scope "provided"]
                          [adzerk/bootlaces "0.2.0" :scope "test"]
                          [boot/core "2.8.2" :scope "provided"]
                          [onetom/boot-lein-generate "0.1.3" :scope "test"]
                          [vivid/ash-ra-template "0.4.0"]]
          :repositories (partial map (fn [[k v]]
                                       [k (cond-> v (#{"clojars"} k) (assoc :username (System/getenv "CLOJARS_USER")
                                                                            :password (System/getenv "CLOJARS_PASS")))])))

(require '[adzerk.bootlaces :refer :all]
         '[boot.test :refer [runtests test-report test-exit]]

         '[vivid.art.boot.tests])
(bootlaces! version)

(task-options!
  pom {:project     project
       :version     version
       :description "Boot task for rendering Ash Ra ART templates."
       :url         "https://github.com/vivid/ash-ra-template"
       :scm         {:url "https://github.com/vivid/ash-ra-template"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}})

; Generate a Leiningen project.clj file for the sake of IntelliJ & Cursive
(require '[boot.lein])
(boot.lein/generate)

(use '[vivid.boot-art])

(deftask deploy
         []
         (comp
           (build-jar)
           (push :repo
                 "clojars"
                 :gpg-sign
                 false)))

(deftask mkdocs
         []
         (comp
           ; The docs require boot-art on the classpath, which is automatically
           ; added to the classpath by Boot (the src/ dir). So we leave it
           ; in Boot's fileset for now ...
           (sift :add-resource #{"assets"})
           (art :dependencies '{boot/core {:mvn/version "2.8.2"}})
           ; ... until the docs are rendered, which is all we are interested in:
           (sift :include #{#"^README\.md$"})
           (target :dir #{"."}
                   :no-clean true
                   :no-link true)))

(deftask test []
         (comp
           (runtests)
           (test-report)
           (test-exit)))
