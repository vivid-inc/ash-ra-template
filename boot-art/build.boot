; Copyright 2019 Vivid Inc.

; Commands:
; boot build-jar         ; Builds and installs the .jar
; boot show --updates    ; Like lein-ancient

; For the sake of the IDE
(require '[boot.core :refer [deftask set-env! task-options!]]
         '[boot.task.built-in :refer [install jar pom]])

(def version "0.5.0")

(set-env! :source-paths #{"test"}
          :resource-paths #{"src"}
          :dependencies '[[adzerk/bootlaces    "0.2.0"   :scope "test"]
                          [boot/core           "2.8.2"   :scope "provided"]
                          [sparkfund/boot-lein "0.4.0"   :scope "test"]
                          [vivid/art           "0.5.0"]]
          :repositories (partial map (fn [[k v]]
                                       [k (cond-> v (#{"clojars"} k) (assoc :username (System/getenv "CLOJARS_USER")
                                                                            :password (System/getenv "CLOJARS_PASS")))])))

(require '[adzerk.bootlaces]
         '[boot.core :as boot]
         '[sparkfund.boot-lein :as boot-lein]
         '[boot.test :refer [runtests test-report test-exit]]
         '[vivid.art.boot-task-test])

(adzerk.bootlaces/bootlaces! version)
(task-options!
  pom {:project     'vivid/boot-art
       :version     version
       :description "Boot task for rendering Ash Ra .art templates"
       :url         "https://github.com/vivid-inc/ash-ra-template"
       :scm         {:url "https://github.com/vivid-inc/ash-ra-template"}
       :license     {"Apache License 2.0"
                     "https://www.apache.org/licenses/LICENSE-2.0"}})

(use '[vivid.boot-art])

(deftask deploy
         []
         (comp
           (pom)
           (jar)
           (target)
           (install)
           (push :repo "clojars"
                 :gpg-sign false)))

(deftask lein-generate
         []
         ; Generate a Leiningen project.clj file for importing the project into an IDE
         ; with the following overrides to make editing a pleasant experience.
         (let [boot-deps (boot/get-env :dependencies)
               overrides (conj boot-deps '[org.clojure/clojure "1.9.0" :scope "provided"])]
           (boot-lein/write-project-clj :override {:dependencies overrides})))

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

(ns-unmap 'boot.user 'test)
(deftask test []
         (comp
           (pom)
           (jar)
           (runtests)
           (test-report)
           (install)
           (test-exit)))
