#!/bin/env bash
set -e

>&2 echo "$0"

# are-we-fast-yet uses different filenames than we do!
TRACKED="Bounce List Mandelbrot NBody Permute Queens Sieve Storage Towers"
RENAMED="bounce list_tail mandelbrot nbody permute queens sieve storage towers"
PREFIX="are_we_fast_yet"

ARR_RENAMED=($RENAMED)

# TODO: Should we somehow sync the arguments here?
# TODO: we could supply a outer and inner loop iteration count to harness.*
# CONFIG_FILE="../../../../../effekt/examples/benchmarks/config_default.txt"

# Hyperfine can only write JSON to files
tmpfile=$(mktemp /tmp/hyperfine_are-we-fast-yet.XXXXX)

# command -> json
benchmark() {
	if hyperfine --export-json "$tmpfile" "$1" &>/dev/null; then
		jq "{mean: .results[0].mean, stddev: .results[0].stddev, arg: 0}" "$tmpfile"
	else
		echo "{\"arg\": 0}"
	fi
}

# output start
echo "{"

# --- JavaScript benchmarks ---
>&2 echo "JavaScript"
cd reference/are-we-fast-yet/benchmarks/JavaScript/

echo "\"js\":"
i=0
{
	for benchmark in $TRACKED; do
		echo "{\"$PREFIX/${ARR_RENAMED[$i]}\":"
		benchmark "node harness.js $benchmark"
		echo "}"
		i=$((i + 1))
	done
} | jq -s 'add'

echo ","

# --- Python benchmarks ---
>&2 echo "Python"
cd ../Python

echo "\"python\":"
i=0
{
	for benchmark in $TRACKED; do
		echo "{\"$PREFIX/${ARR_RENAMED[$i]}\":"
		benchmark "python3 harness.py $benchmark"
		echo "}"
		i=$((i + 1))
	done
} | jq -s 'add'

echo ","

# --- Java benchmarks ---
>&2 echo "Java"
cd ../Java

./build.sh >&2

echo "\"java\":"
i=0
{
	for benchmark in $TRACKED; do
		echo "{\"$PREFIX/${ARR_RENAMED[$i]}\":"
		benchmark "java -cp benchmarks.jar Harness $benchmark 1"
		echo "}"
		i=$((i + 1))
	done
} | jq -s 'add'

echo ","

# --- C++ benchmarks ---
>&2 echo "C++"
cd ../C++

# generated using the build.sh script -- TODO: Do we want to change this?
clang++ -Wall -Wextra -Wno-unused-private-field -O3 -flto -march=native -ffp-contract=off -std=c++17 src/harness.cpp src/deltablue.cpp src/memory/object_tracker.cpp src/richards.cpp -o harness >&2

echo "\"cpp\":"
i=0
{
	for benchmark in $TRACKED; do
		echo "{\"$PREFIX/${ARR_RENAMED[$i]}\":"
		benchmark "./harness $benchmark"
		echo "}"
		i=$((i + 1))
	done
} | jq -s 'add'

# --- Done ---

# output end
echo "}"
rm "$tmpfile"
