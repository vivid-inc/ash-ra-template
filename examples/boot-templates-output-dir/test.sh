#!/usr/bin/env bash

set -o errexit
set -o xtrace

rm -r artifax || true

boot rndr
diff -r artifax/ expected/
