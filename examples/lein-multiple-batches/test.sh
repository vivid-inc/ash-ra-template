#!/usr/bin/env bash

set -o errexit
set -o xtrace

rm -r target || true

lein art
diff -r expected-src-resources/ src/resources/
diff -r expected-target-generated-sources-java/ target/generated-sources/java/
