#!/usr/bin/env bash
# Copyright 2021 Vivid Inc.

# This tool is meant to be used from the project root, as so:
#     $ bin/gen.clj

set -o errexit
set -o xtrace

function bootstrap_art {
  clojure -Sdeps '{:deps {vivid/ash-ra-template {:mvn/version "0.5.0"
                                                 :local/root  "art"}
                          zprint {:mvn/version "1.0.2"}}}' - <<EOS

(require '[vivid.art])

(def ^:const vivid-art-version "0.5.0")

(defn rndr [from to opts]
  (as-> (slurp from) c
        (vivid.art/render c opts)
        (spit to c)))

(rndr "art/assets/project.clj.art" "art/project.clj"
      {:bindings     {'vivid-art-version vivid-art-version}
       :dependencies {'zprint {:mvn/version "1.0.2"}}})

(rndr "art/assets/README.md.art" "art/README.md"
      {:bindings     {'vivid-art-version vivid-art-version}
       :delimiters   {:begin-forms "{%"
                      :end-forms   "%}"
                      :begin-eval  "{%="
                      :end-eval    "%}"}
       :dependencies {'vivid/art {:mvn/version "0.5.0"}}})

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
