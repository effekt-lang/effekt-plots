#!/bin/env bash
set -e

DATA=../data

# gather meta information
cd ../effekt/
commit="$(git rev-parse --short HEAD)"
commitDate="$(git show --no-patch --format=%ct "$commit")"
currentDate=$(date +%s)
cd -

annotated=$(jq --arg commit "$commit" --arg commitDate "$commitDate" --arg currentDate "$currentDate" \
	'. + {meta: {commit: $commit, commitDate: $commitDate, currentDate: $currentDate}}' - <&0)

# append to existing data
echo "$annotated" | jq '(. += [input]) | sort_by(.meta.commitDate)' "$DATA"/"$1" - >"$1".temp
mv "$1.temp" "$DATA"/"$1" # sponge
