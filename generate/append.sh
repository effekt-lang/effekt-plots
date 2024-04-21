#!/bin/env bash
set -e

DATA=../data

# gather meta information
cd ../effekt/
commit="$(git rev-parse --short HEAD)"
date="$(git show --no-patch --format=%ci "$commit")"
cd -

annotated=$(jq --arg commit "$commit" --arg date "$date" '. + {meta: {commit: $commit, date: $date}}' - <&0)

# append to existing data
echo "$annotated" | jq '. += [input]' "$DATA"/"$1" - >"$1".temp
mv "$1.temp" "$DATA"/"$1" # sponge
