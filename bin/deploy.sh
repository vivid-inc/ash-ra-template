#!/usr/bin/env bash

# Referencing https://github.com/clojars/clojars-web/wiki/Pushing

set -o errexit
set -o xtrace

lein deploy clojars
