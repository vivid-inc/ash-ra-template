#!/usr/bin/env bash
# Copyright 2021 Vivid Inc.

# This tool is meant to be used from the project root, as so:
#     $ bin/gen.clj

set -o errexit
set -o nounset
set -o pipefail
set -o xtrace

function bootstrap_art {
  clojure -Sdeps '{:deps {vivid/ash-ra-template {:mvn/version "0.6.0"
                                                 :local/root  "art"}
                          zprint {:mvn/version "1.0.2"}}}' - <<EOS

(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[vivid.art])
(import '(java.io PushbackReader))

(def ^:const vivid-art-facts (with-open [r (io/reader "assets/vivid-art-facts.edn")]
                               (edn/read (PushbackReader. r))))
(def ^:const base-rndr-opts {:bindings vivid-art-facts})

(def ^:const batches [
  "art/assets/project.clj.art" "art/project.clj"
  {:dependencies {'zprint {:mvn/version "1.0.2"}}}

  "art/assets/README.md.art" "art/README.md"
  {:delimiters   {:begin-forms "{%"
                  :end-forms   "%}"
                  :begin-eval  "{%="
                  :end-eval    "%}"}
   :dependencies {'vivid/art {:mvn/version ('vivid-art-version vivid-art-facts)}
                  'zprint    {:mvn/version "1.0.2"}}}
])

(defn rndr [from to opts-overrides]
  (println "Rendering ART" to)
  (let [opts* (merge base-rndr-opts opts-overrides)]
    (as-> (slurp from) c
          (vivid.art/render c opts*)
          (spit to c))))

(doseq [batch (partition 3 batches)]
  (apply rndr batch))

EOS
}

echo Generating resources in all projects

# Generate art/ project files, then install the ART .jar into the local Maven
# repository, making it available to subsequent build steps that depend on it.
bootstrap_art
(cd art && lein install)

(cd boot-art && boot lein-generate mkdocs)
(cd clj-art  && clojure -A:mkdocs)
(cd lein-art && lein mkdocs)
