#!/bin/env bash
set -e

>&2 echo "$0"
>&2 cat <&0
exit 1

DATA=../data

# gather meta information
cd ../effekt/
commit="$(git rev-parse --short HEAD)"
commitDate="$(git show --no-patch --format=%ct "$commit")"
currentDate=$(date +%s)
cd -

annotated=$(jq --arg commit "$commit" --arg commitDate "$commitDate" --arg currentDate "$currentDate" \
	'. + {meta: {commit: $commit, commitDate: $commitDate, currentDate: $currentDate}}' - <&0)

# split data dumps by year+month
currentMonth=$(date +%Y%m)
outFile="$DATA/$1/$currentMonth.json"

if [ ! -f "$outFile" ]; then
	echo "[]" >"$outFile"
fi

# append to existing data
echo "$annotated" | jq '(. += [input]) | sort_by(.meta.commitDate)' "$outFile" - >"$1".temp
mv "$1.temp" "$outFile" # sponge
