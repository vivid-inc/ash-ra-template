#!/usr/bin/env bash

# Referencing https://github.com/clojars/clojars-web/wiki/Deploy-Tokens
# Referencing https://github.com/clojars/clojars-web/wiki/Pushing

set -o errexit
set -o nounset
set -o pipefail
set -o xtrace

lein deploy clojars
