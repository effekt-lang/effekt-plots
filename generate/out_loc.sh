#!/bin/env bash
set -e

>&2 echo "$0"

# NOTE: this depends on built llvm/js benchmarks (e.g. by running `./benchmarks.sh`)

cd ../effekt/out/

{
	echo "{\"llvm\":"
	cloc --by-file --json $(ls *.ll | sed '/\.opt\.ll/d') | jq 'del(.header)'
	echo "}"

	echo "{\"js\":"
	cloc --by-file --json *.js | jq 'del(.header)'
	echo "}"

	echo "{\"chez-cps\":"
	cloc --by-file --json *.ss | jq 'del(.header)'
	echo "}"
} | jq -s 'add'
