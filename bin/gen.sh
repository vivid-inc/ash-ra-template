#!/usr/bin/env bash

set -o errexit
set -o xtrace

echo Generating resources in all projects

# Project files. Install the ART .jar into Maven's repository, making
# it available to subsequent build steps that depend on it.
(cd art && ./gen-project.clj && lein install)

(cd boot-art && boot mkdocs)
(cd clj-art  && clojure -A:mkdocs)
(cd lein-art && lein mkdocs)
