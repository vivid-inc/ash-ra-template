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
                          net.vivid-inc/art-cli {:local/root "art-cli"}
                          net.vivid-inc/clj-art {:local/root "clj-art"}}}' - <<EOS

(require '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[vivid.art.cli.args]
         '[vivid.art.cli.exec])
(import '(java.io PushbackReader))

(def ^:const asset-dirs [
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
  (map ->b asset-dirs))

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
