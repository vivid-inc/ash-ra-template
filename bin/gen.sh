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
#     $ bin/gen.sh

set -o errexit
set -o nounset
set -o pipefail
set -o xtrace

function bootstrap_art {
  clojure -Sdeps '{:deps {org.suskalo/farolero  {:mvn/version "1.5.0"}
                          net.vivid-inc/art     {:local/root "art"}
                          net.vivid-inc/art-cli {:local/root "art-cli"}
                          net.vivid-inc/clj-art {:local/root "clj-art"}}}' - <<EOS

(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[vivid.art.cli :as art-cli]
         '[vivid.art.cli.args])
(import '(java.io PushbackReader))

(def ^:const component-dirs [
  ".circleci"
  "art"
  "art-cli"
  "clj-art"
  "lein-art"])

(defn ->b [dir]
  (let [templates (->> (file-seq (clojure.java.io/file (str dir "/assets/")))
                       (filter #(not (.isDirectory %)))
                       (map #(.getPath %))
                       (filter #(re-find vivid.art.cli/art-filename-suffix-regex %)))]
    {:templates  templates
     :output-dir dir
     :bindings   "assets/vivid-art-facts.edn"
     :delimiters "erb"}))

(def ^:const raw-batches
  (map ->b component-dirs))

(farolero.core/handler-case
  (doseq [b raw-batches]
    (art-cli/render-batches
      [(vivid.art.cli.args/direct->batch (:templates b) b)]))
  (:vivid.art.cli/error [_ details] (do (prn details)
                                        (System/exit 1))))

EOS
}

echo Generating resources in all projects

# Generate art/ project files, then install the ART .jar into the local Maven
# repository, making it available to subsequent build steps that depend on it.
bootstrap_art
(cd art && lein do pom, install)
(cd art-cli && lein do pom, install)
(cd clj-art && lein pom && clojure -M:gen)
(cd lein-art && lein do pom, gen)
