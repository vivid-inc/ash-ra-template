#!/usr/bin/env bash

set -e
set -x

echo Generating resources in all projects

# Project files
(cd ash-ra-template && ./gen-project.clj)

# Documentation
(cd boot-art && boot mkdocs)
(cd lein-art && lein mkdocs)
