#!/bin/env bash
set -e

>&2 echo "$0"

BACKENDS="llvm js"

merge() {
	backend=$1
	config=$2

	if [ "$config" = "default" ]; then
		name="${backend}_default"
	elif [ "$config" = "reference" ]; then
		name="${backend}_reference"
	else
		name=$backend
	fi

	echo "{\"$name\":"
	{
		log="../effekt/benchmarks_${backend}_${config}.log"

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
		merge "$backend" "$backend"
	done

	# now do the same for default arguments
	for backend in $BACKENDS; do
		merge "$backend" "default"
	done

	# and reference benchmarks (only JS for now)
	merge "js" "reference"
} | jq -s 'add'
