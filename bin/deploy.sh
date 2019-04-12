#!/usr/bin/env bash

# Referencing https://github.com/clojars/clojars-web/wiki/Pushing

set -e
set -x

lein deploy clojars
