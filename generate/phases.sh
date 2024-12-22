#!/bin/env bash
set -e
set -x

jq -s 'reduce .[] as $item ({}; . * $item)' ../effekt/out/*.json
rm -rf out/
