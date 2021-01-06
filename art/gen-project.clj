#!/usr/bin/env bash
# Copyright 2020 Vivid Inc.

clojure -Sdeps '{:deps {zprint {:mvn/version "1.0.2"}}} ' - <<EOS
(require 'vivid.art)
(as-> (slurp "assets/README.md.art") c
      (vivid.art/render c {:dependencies '{zprint {:mvn/version "1.0.2"}}})
      (spit "project.clj" c))
EOS
