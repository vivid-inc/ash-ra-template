#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail
set -o xtrace

CLOJURE_VERSIONS=$(lein run -m clojure.main -e \
  '(print (as-> "assets/vivid-art-facts.edn" d
                (with-open [r (clojure.java.io/reader d)]
                  (clojure.edn/read (java.io.PushbackReader. r)))
                (get d "clojure-versions")
                (clojure.string/join " " d)))')

echo Running all tests

export TZ=UTC

# Aim for a clean build
find . -depth -name .cpcache -or -name out -or -name target | xargs rm -r || true

# Run all tests, create the deliverables
(cd art && lein test)
(cd art-cli && lein test)
(cd boot-art && for ver in "${CLOJURE_VERSIONS[@]}" ; do export BOOT_CLOJURE_VERSION=${ver} ; boot test ; done)
(cd clj-art && lein install && for ver in "${CLOJURE_VERSIONS[@]}" ; do clojure -M:clojure-${ver}:test ; done)
(cd lein-art && lein install && lein test)
