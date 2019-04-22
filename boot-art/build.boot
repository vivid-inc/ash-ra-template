; For the sake of IntelliJ & Cursive
(require '[boot.core :refer [deftask set-env! task-options!]]
         '[boot.task.built-in :refer [install jar pom]])

(def project 'vivid/boot-art)
(def version "0.3.0")

(set-env! :source-paths #{"src"}
          :resource-paths #{"src"}
          :dependencies '[[org.clojure/clojure "1.9.0" :scope "provided"]
                          [adzerk/bootlaces "0.2.0" :scope "test"]
                          [boot/core "2.8.3" :scope "provided"]
                          [onetom/boot-lein-generate "0.1.3" :scope "test"]
                          [vivid/ash-ra-template "0.3.0"]]
          :repositories (partial map (fn [[k v]]
                                       [k (cond-> v (#{"clojars"} k) (assoc :username (System/getenv "CLOJARS_USER")
                                                                            :password (System/getenv "CLOJARS_PASS")))])))

(require '[adzerk.bootlaces :refer :all])
(bootlaces! version)

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

(deftask deploy
         []
         (comp (build-jar)
               (push :repo
                     "clojars"
                     :gpg-sign
                     false)))

(require '[vivid.art.boot :refer [art]])
