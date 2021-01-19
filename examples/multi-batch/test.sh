#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail
set -o xtrace

rm -r target || true

function clj-art {
    clj -A:rndr-a
    clj -A:rndr-b
}

# Hack: If the command-line args are "clj-art", the following will execute
# the Bash function defined above.
# Where clj-tool-test is written to support the running of just one command,
# the clj-art() function above quarantines the one-off complexity to this script.
$@
diff -r expected-src-resources/ src/resources/
diff -r expected-target-generated-sources-java/ target/generated-sources/java/
