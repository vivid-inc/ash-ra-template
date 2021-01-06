#!/usr/bin/env bash

set -o errexit
set -o xtrace

echo Running all tests

export TZ=UTC

# Aim for a clean build
find . -depth -name target | xargs rm -r || true

# Run all tests, create the deliverables
(cd art && lein test-all)
(cd boot-art && boot test-all)
(cd clj-art && lein test-all)
(cd lein-art && lein test-all)
