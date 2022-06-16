#!/usr/bin/env bash
# Copyright 2022 Vivid Inc.

# This tool is meant to be used from the project root, as so:
#     $ bin/gen.sh

set -o errexit
set -o nounset
set -o pipefail
set -o xtrace

function bootstrap_art {
  clojure -Sdeps '{:deps {org.suskalo/farolero  {:mvn/version "1.4.3"}
                          net.vivid-inc/art     {:local/root "art"}
                          net.vivid-inc/art-cli {:local/root "art-cli"}}}' - <<EOS

(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[vivid.art.cli.args]
         '[vivid.art.cli.exec])
(import '(java.io PushbackReader))

(def ^:const vivid-art-facts (with-open [r (io/reader "assets/vivid-art-facts.edn")]
                               (edn/read (PushbackReader. r))))

(def ^:const build-files [
  "art"      ["assets/project.clj.art"]
  "art-cli"  ["assets/project.clj.art"]
  "clj-art"  ["assets/deps.edn.art" "assets/project.clj.art"]
  "lein-art" ["assets/project.clj.art"]])

(defn ->b [[dir templates]]
  {:templates (map #(str dir "/" %) templates)
   :output-dir dir
   :bindings   vivid-art-facts
   :delimiters "erb"})

(def ^:const raw-batches
  (->> (partition 2 build-files)
       (map ->b)))

(farolero.core/handler-case
  (doseq [b raw-batches]
    (vivid.art.cli.exec/render-batches-once
      [(vivid.art.cli.args/direct->batch (:templates b) b)]))
  (:vivid.art.cli/error [_ details] (do (prn details)
                                        (System/exit 1))))

EOS
}

echo Generating resources in all projects

# Generate art/ project files, then install the ART .jar into the local Maven
# repository, making it available to subsequent build steps that depend on it.
bootstrap_art
(cd art && lein install)
(cd art-cli && lein install)
(cd clj-art  && clojure -M:gen)
(cd lein-art && lein gen)
