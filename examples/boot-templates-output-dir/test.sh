#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail
set -o xtrace

rm -r artifax || true

boot rndr
diff -r artifax/ expected/
