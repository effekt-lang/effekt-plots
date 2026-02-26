#!/bin/env bash
set -e

>&2 echo "$0"

BACKENDS="llvm js chez-cps"

merge() {
	backend=$1

	echo "{\"$backend\":"
	{
		log="../effekt/benchmarks_${backend}.log"

		while read data; do
			arr=($data)
			echo "{\"${arr[0]}\": {\"maxMem\": ${arr[3]}, \"time\": ${arr[2]}, \"arg\": ${arr[1]}}}"
		done <"$log"
	} | jq -s 'add'
	echo "}"
}

{
	# merge benchmark data for all backends
	for backend in $BACKENDS; do
		merge "$backend"
	done

	# and reference benchmarks (only JS for now)
	merge "js" "reference"
} | jq -s 'add'
