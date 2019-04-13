#!/usr/bin/env bash

set -e
set -x

rm -r target

lein build
