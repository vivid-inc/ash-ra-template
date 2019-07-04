

(defproject
  vivid/ash-ra-template "0.4.0"

  :note "THIS FILE IS AUTOMATICALLY GENERATED BY gen-project.clj"
  :description "Ash Ra Template: Expressive template system for Clojure."
  :url "https://github.com/vivid-inc/ash-ra-template"
  :license {:distribution :repo
            :name         "Eclipse Public License"
            :url          "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[instaparse "1.4.10" :exclusions [org.clojure/spec.alpha]]
                 [special "0.1.3-Beta1"]
                 [org.clojure/tools.deps.alpha "0.7.516" :exclusions [commons-logging
                                                                      org.slf4j/slf4j-api]]
                 [org.projectodd.shimdandy/shimdandy-api "1.2.1"]
                 [org.projectodd.shimdandy/shimdandy-impl "1.2.1"]
                 [org.xeustechnologies/jcl-core "2.8"]
                 [reduce-fsm "0.1.4"]]

  :aliases {"build"    ["do"
                        "version,"
                        "clean,"
                        "cloverage,"
                        "eastwood,"
                        "jar,"
                        "install"]
            "test-all" ["with-profile" "+clojure-1.9.0:+clojure-1.10.0:+clojure-1.10.1" "build"]}

  :cloverage {:codecov? true
              :html?    false
              :output   "."                                 ; "lein jar" appears to destroy target/coverage/codecov.json
              }

  ;test/vivid/art/api_contract_test.clj:70:5: unused-ret-vals-in-try: Return value is discarded for a function call that only has side effects if the functions passed to it as args have side effects inside body of try: (apply art/failure? [])
  :eastwood {:exclude-linters []
             :namespaces      [:source-paths]}

  :exclusions [org.clojure/clojure]

  :global-vars {*warn-on-reflection* true}

  :javac-options ["-target" "1.8"]

  :manifest {"Built-By" "vivid"}

  :min-lein-version "2.9.1"

  ; Note: Enable only to more strictly inspect dependencies
  ;:pedantic? :abort

  :plugins [[jonase/eastwood "0.3.5"]
            [lein-ancient "0.6.15"]
            [lein-cloverage "1.1.1"]
            [lein-kibit "0.1.6"]
            [lein-nvd "1.1.0" :exclusions [org.slf4j/jcl-over-slf4j
                                           org.slf4j/slf4j-api]]
            [venantius/yagni "0.1.7"]]

  :profiles {:dev
 {:dependencies [[pjstadig/humane-test-output "0.9.0"]],
  :injections
  [(require 'pjstadig.humane-test-output)
   (pjstadig.humane-test-output/activate!)
   (require 'vivid.art)
   (require 'clojure.spec.test.alpha)
   (clojure.spec.test.alpha/instrument)],
  :plugins [[com.jakemccrary/lein-test-refresh "0.24.1"]],
  :resource-paths ["test-resources"],
  :test-refresh {:quiet true}},
 :clojure-1.9.0 {:dependencies [[org.clojure/clojure "1.9.0"]]},
 :clojure-1.10.0 {:dependencies [[org.clojure/clojure "1.10.0"]]},
 :clojure-1.10.1 {:dependencies [[org.clojure/clojure "1.10.1"]]}}


  :repositories [["clojars" {:sign-releases false}]])
