#!/bin/env bash
set -e

>&2 echo "$0"

if [ "$1" = "sync" ]; then
	# generate reference data per request
	rm -f ../data/reference.json
	{
		./reference/are-we-fast-yet.sh
		./reference/duality-of-compilation.sh
		./reference/effect-handlers-bench.sh
	} | jq -s 'reduce .[] as $x ({}; . * $x)' >../data/reference.json
else
	# effekt data
	./reference/effekt.sh
fi
