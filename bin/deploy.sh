#!/usr/bin/env bash
# Copyright 2022 Vivid Inc.

# This tool is meant to be used from the project root, as so:
#     $ bin/deploy.sh

# Referencing https://github.com/clojars/clojars-web/wiki/Deploy-Tokens
# Referencing https://github.com/clojars/clojars-web/wiki/Pushing

set -o errexit
set -o nounset
set -o pipefail
set -o xtrace

export TZ=UTC
for DIR in art art-cli boot-art clj-art lein-art
do
  (cd $DIR && lein deploy clojars)
done
