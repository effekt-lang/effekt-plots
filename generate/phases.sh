#!/bin/env bash
set -e

>&2 echo "$0"

jq -s 'reduce .[] as $item ({}; . * $item)' ../effekt/out/*.json
rm -rf out/
