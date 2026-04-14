#!/bin/env bash
set -e

>&2 echo "$0"

TRIALS=10
QUERY='NOT is:fork NOT user:effekt-lang extension:effekt'

# GH api sometimes returns smaller numbers, so we run a few times and take the maximum, lol
max_count=0
for _ in $(seq $TRIALS); do
	count=$(gh api -X GET search/code -f q="$QUERY" -f per_page=1 | jq -r '.total_count')
	if ((count > max_count)); then
		max_count=$count
	fi
	sleep 6 # 10 requests/min = 1 per 6s
done

jq -n \
	--arg count "$max_count" \
	'{count_github: $count}'
