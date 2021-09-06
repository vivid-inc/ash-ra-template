#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail
set -o xtrace

CLOJURE_VERSIONS=( "1.9.0" "1.10.0" "1.10.1" "1.10.2" "1.10.3" )

echo Running all tests

export TZ=UTC

# Aim for a clean build
find . -depth -name out -or -name target | xargs rm -r || true

# Run all tests, create the deliverables
(cd art && lein test)
(cd boot-art && for ver in "${CLOJURE_VERSIONS[@]}" ; do export BOOT_CLOJURE_VERSION=${ver} ; boot test ; done)
(cd clj-art && lein install && for ver in "${CLOJURE_VERSIONS[@]}" ; do clojure -M:clojure-${ver}:test ; done)
(cd lein-art && lein install && lein test)
