#!/bin/env bash
set -e

jq -s 'reduce .[] as $item ({}; . * $item)' ../effekt/out/*.json
rm -rf out/
