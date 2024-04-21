#!/bin/env bash
set -e

jq -s 'reduce .[] as $item ({}; . * $item)' out/*.json
rm -rf out/
