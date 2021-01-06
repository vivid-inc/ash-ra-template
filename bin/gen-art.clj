#!/usr/bin/env bash
# Copyright 2021 Vivid Inc.

# This tool is meant to be used from the project root, as so:
#     $ bin/gen-art.clj

clojure -Sdeps '{:deps {vivid/ash-ra-template {:mvn/version "0.4.0"
                                               :local/root  "art"}
                        zprint {:mvn/version "1.0.2"}}}' - <<EOS

(require '[vivid.art])

(defn rndr [from to opts]
  (as-> (slurp from) c
        (vivid.art/render c opts)
        (spit to c)))

(rndr "art/assets/project.clj.art" "art/project.clj"
      {:dependencies {'zprint {:mvn/version "1.0.2"}}})
(rndr "art/assets/README.md.art" "art/README.md"
      {:dependencies {'vivid/art {:mvn/version "0.5.0"}}
       :delimiters {:begin-forms "{%"
                    :end-forms   "%}"
                    :begin-eval  "{%="
                    :end-eval    "%}"}})

EOS
