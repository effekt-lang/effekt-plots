#!/bin/env bash
set -e

DATA=../data

# gather meta information
cd ../effekt/
commit="$(git rev-parse --short HEAD)"
date="$(git show --no-patch --format=%ci "$commit")"
cd -

if grep -q "$commit" "$DATA"/"$1"; then
	echo "commit $commit already measured!"
	exit 1
fi

annotated=$(jq --arg commit "$commit" --arg date "$date" '. + {meta: {commit: $commit, date: $date}}' - <&0)

# append to existing data
echo "$annotated" | jq '(. += [input]) | sort_by(.meta.date)' "$DATA"/"$1" - >"$1".temp
mv "$1.temp" "$DATA"/"$1" # sponge
