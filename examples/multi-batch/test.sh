#!/usr/bin/env bash

# Copyright 2023 Vivid Inc. and/or its affiliates.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -o errexit
set -o nounset
set -o pipefail
set -o xtrace

rm -r target || true

function clj-art {
    clojure -M:rndr-a
    clojure -M:rndr-b
}

# Hack: If the command-line args are "clj-art", the following will execute
# the Bash function defined above.
# Where clj-tool-test is written to support the running of just one command,
# the clj-art() function above quarantines the one-off complexity to this script.
$@
diff -r expected-src-resources/ src/resources/
diff -r expected-target-generated-sources-java/ target/generated-sources/java/
diff -r expected-www/ www/