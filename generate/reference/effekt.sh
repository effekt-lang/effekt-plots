#!/bin/env bash
set -e

>&2 echo "::group::$0"

cd ../effekt/

BACKENDS="llvm js"

# Hyperfine can only write JSON to files
tmpfile=$(mktemp /tmp/hyperfine_effekt.XXXXX)

benchmark() {
	if hyperfine --export-json "$tmpfile" --show-output "$1" >&2; then
		jq "{mean: .results[0].mean, stddev: .results[0].stddev, arg: $2}" "$tmpfile"
	else
		echo "{\"arg\": $2}"
	fi
}

{
	for backend in $BACKENDS; do
		echo "{\"effekt_$backend\":"
		{
			while read config; do
				arr=($config)
				filename=${arr[0]%.*}
				file="examples/benchmarks/$filename.effekt"
				outfile="out/$(basename "$file" .effekt)"

				echo "{\"$filename\":"
				effekt.sh --backend "$backend" -b "$file" >&2
				arg=${arr[1]}
				benchmark "./$outfile $arg" "$arg"
				echo "}"
			done <"examples/benchmarks/config_default.txt"
		} | jq -s 'add'
		echo "}"
	done
} | jq -s 'add'

rm "$tmpfile"

>&2 echo "::endgroup::"
