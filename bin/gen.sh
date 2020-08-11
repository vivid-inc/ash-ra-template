#!/usr/bin/env bash

set -e
set -x

echo Generating resources in all projects

# Project files. Install the ART .jar into Maven's repository, making
# it available to subsequent build steps that depend on ART.
(cd ash-ra-template && ./gen-project.clj && lein install)

# Documentation
(cd boot-art && boot mkdocs)
(cd lein-art && lein mkdocs)
