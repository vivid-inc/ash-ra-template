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

# This tool is meant to be used from the project root, as so:
#     $ bin/test.sh

set -o errexit
set -o nounset
set -o pipefail
set -o xtrace

IFS=' ' read -a CLOJURE_VERSIONS <<< $(lein run -m clojure.main -e \
  '(print (as-> "assets/vivid-art-facts.edn" d
                (with-open [r (clojure.java.io/reader d)]
                  (clojure.edn/read (java.io.PushbackReader. r)))
                (get d (symbol "clojure-versions"))
                (clojure.string/join " " d)))')

echo Running all tests

export TZ=UTC

# Aim for a clean build
find . -depth -name .cpcache -or -name out -or -name target -type d | xargs rm -r || true

# Run all tests, create the deliverables
(cd art && lein test)
(cd art-cli && lein test)
(cd clj-art && lein install && for ver in "${CLOJURE_VERSIONS[@]}" ; do clojure -M:clojure-${ver}:test ; done)
(cd lein-art && lein install && lein test)
