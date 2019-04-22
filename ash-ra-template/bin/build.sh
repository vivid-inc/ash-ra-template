#!/usr/bin/env bash

set -e
set -x

rm -rf target

TZ=UTC lein build
