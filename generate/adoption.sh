#!/bin/env bash
set -e

>&2 echo "$0"

query='NOT is:fork NOT user:effekt-lang extension:effekt'

# one dry run (paginating forces github to find new data, maybe)
gh api -X GET --paginate search/code -f q="$query" &>/dev/null
sleep 5

count="$(gh api -X GET search/code -f q="$query" | jq -r '.total_count')"

jq -n \
	--arg count "$count" \
	'{count_github: $count}'
