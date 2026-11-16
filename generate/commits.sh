#!/bin/env bash
set -e

git -C ../effekt log --all --format='%H%x09%ct%x09%s' |
    jq -Rnc '[inputs | split("\t") | {hash: .[0], date: (.[1] | tonumber), message: (.[2:] | join("\t"))}] | sort_by(.date)'
