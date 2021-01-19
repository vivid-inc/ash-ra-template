#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail
set -o xtrace

CLJ_VERSIONS=( "1.9.0" "1.10.0" "1.10.1" )

echo Running all tests

export TZ=UTC

# Aim for a clean build
find . -depth -name target | xargs rm -r || true

# Run all tests, create the deliverables
(cd art && lein test)
(cd boot-art && for ver in "${CLJ_VERSIONS[@]}" ; do export BOOT_CLOJURE_VERSION=${ver} ; boot test ; done)
(cd clj-art && for ver in "${CLJ_VERSIONS[@]}" ; do clojure -A:clojure-${ver}:test ; done)
(cd lein-art && lein test)
