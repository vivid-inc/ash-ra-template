#!/usr/bin/env bash
# Copyright 2022 Vivid Inc.

# This tool is meant to be used from the project root, as so:
#     $ bin/test.sh

set -o errexit
set -o nounset
set -o pipefail
set -o xtrace

IFS=' ' read -a CLOJURE_VERSIONS <<< $(lein run -m clojure.main -e \
  '(print (as-> "assets/vivid-art-facts.edn" d
                (with-open [r (clojure.java.io/reader d)]
                  (clojure.edn/read (java.io.PushbackReader. r)))
                (get d (symbol "clojure-versions"))
                (clojure.string/join " " d)))')

echo Running all tests

export TZ=UTC

# Aim for a clean build
find . -depth -name .cpcache -or -name out -or -name target | xargs rm -r || true

# Run all tests, create the deliverables
(cd art && lein test)
(cd art-cli && lein test)
(cd clj-art && lein install && for ver in "${CLOJURE_VERSIONS[@]}" ; do clojure -M:clojure-${ver}:test ; done)
(cd lein-art && lein install && lein test)
