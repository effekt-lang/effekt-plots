#!/bin/env bash
set -e

>&2 echo "$0"

TRACKED="erase_unused factorial_accumulator fibonacci_recursive iterate_increment lookup_tree match_options sum_range"
PREFIX="duality_of_compilation"

CONFIG_FILE="../../../../effekt/examples/benchmarks/config_default.txt"

# Hyperfine can only write JSON to files
tmpfile=$(mktemp /tmp/hyperfine_duality-of-computation.XXXXX)

# name -> number
get_arg() {
	grep "$PREFIX/$1" "$CONFIG_FILE" | awk '{print $2}'
}

# command -> arg -> json
benchmark() {
	if hyperfine --export-json "$tmpfile" "$1" &>/dev/null; then
		jq "{mean: .results[0].mean, stddev: .results[0].stddev, arg: $2}" "$tmpfile"
	else
		echo "{\"arg\": $2}"
	fi
}

# output start
echo "{"

# --- Rust benchmarks ---

cd reference/duality-of-compilation/rust/

make &>/dev/null

echo "\"rust\":"
i=0
{
	for benchmark in $TRACKED; do
		echo "{\"$PREFIX/$benchmark\":"
		arg=$(get_arg "$benchmark")
		benchmark "./$benchmark $arg" "$arg"
		echo "}"
	done
} | jq -s 'add'

echo ","

# --- Koka benchmarks ---

cd ../koka/

# TODO: make this smarter (for CI)
make KOKA_HOME=/usr/local/share/koka/v3.1.2/ &>/dev/null

echo "\"koka\":"
i=0
{
	for benchmark in $TRACKED; do
		echo "{\"$PREFIX/$benchmark\":"
		arg=$(get_arg "$benchmark")
		benchmark "./$benchmark $arg" "$arg"
		echo "}"
	done
} | jq -s 'add'

echo ","

# --- Ocaml benchmarks ---

cd ../ocaml/

make &>/dev/null

echo "\"ocaml\":"
i=0
{
	for benchmark in $TRACKED; do
		echo "{\"$PREFIX/$benchmark\":"
		arg=$(get_arg "$benchmark")
		benchmark "./$benchmark $arg" "$arg"
		echo "}"
	done
} | jq -s 'add'

# --- Done ---

# output end
echo "}"
rm "$tmpfile"
