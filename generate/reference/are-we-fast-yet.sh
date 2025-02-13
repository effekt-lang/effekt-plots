#!/bin/env bash
set -e

# TODO: we could supply a outer and inner loop iteration count to harness.*

# are-we-fast-yet uses different filenames than we do!
TRACKED="Bounce List Mandelbrot NBody Permute Queens Sieve Storage Towers"
RENAMED="bounce list_tail mandelbrot nbody permute queens sieve storage towers"
PREFIX="are_we_fast_yet"

ARR_RENAMED=($RENAMED)

# Hyperfine can only write JSON to files
tmpfile=$(mktemp /tmp/hyperfine_are-we-fast-yet.XXXXX)

# command -> json
benchmark() {
	hyperfine --export-json "$tmpfile" "$1" &>/dev/null
	jq "{mean: .results[0].mean, stddev: .results[0].stddev}" "$tmpfile"
}

# output start
echo "{"

# --- JavaScript benchmarks ---

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

cd ../Python

echo "\"python\":"
i=0
{
	for benchmark in $TRACKED; do
		echo "{\"$PREFIX/${ARR_RENAMED[$i]}\":"
		benchmark "python harness.py $benchmark"
		echo "}"
		i=$((i + 1))
	done
} | jq -s 'add'

echo ","

# --- C++ benchmarks ---

cd ../C++

# generated using the build.sh script -- TODO: Do we want to change this?
clang++ -Wall -Wextra -Wno-unused-private-field -O3 -flto -march=native -ffp-contract=off -std=c++17 src/harness.cpp src/deltablue.cpp src/memory/object_tracker.cpp src/richards.cpp -o harness &>/dev/null

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
