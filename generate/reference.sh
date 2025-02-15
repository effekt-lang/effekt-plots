#!/bin/env bash
set -e

>&2 echo "$0"

# generate reference data per request
if [ "$1" = "sync" ]; then
	{
		./reference/are-we-fast-yet.sh
		./reference/duality-of-compilation.sh
		./reference/effect-handlers-bench.sh
	} | jq -s 'reduce .[] as $x ({}; . * $x)' >../data/reference.json
fi

# effekt data
./reference/effekt.sh
