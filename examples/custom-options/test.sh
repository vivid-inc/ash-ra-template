#!/usr/bin/env bash

set -o errexit
set -o xtrace

rm -r out pom.xml target || true

$@
diff -r expected/ out/cdn/
