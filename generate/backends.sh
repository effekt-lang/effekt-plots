#!/bin/env bash
set -e

>&2 echo "$0"

BACKENDS="llvm js"

{
	for backend in $BACKENDS; do
		echo "{\"$backend\":"
		{
			log="../effekt/benchmarks_$backend.log"

			while read data; do
				arr=($data)
				echo "{\"${arr[0]}\": {\"maxMem\": ${arr[3]}, \"time\": ${arr[2]}, \"arg\": ${arr[1]}}}"
			done <"$log"
		} | jq -s 'add'
		echo "}"
	done
} | jq -s 'add'
