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
#     $ bin/deploy.sh

# Referencing https://github.com/clojars/clojars-web/wiki/Deploy-Tokens
# Referencing https://github.com/clojars/clojars-web/wiki/Pushing

set -o errexit
set -o nounset
set -o pipefail
set -o xtrace

export TZ=UTC
for DIR in art art-cli clj-art lein-art
do
  (cd $DIR && lein deploy clojars)
done
