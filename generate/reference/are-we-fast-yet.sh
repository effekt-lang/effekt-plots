#!/bin/env bash
set -e

ulimit -s 4194304

>&2 echo "::group::$0"

# are-we-fast-yet uses different filenames than we do!
TRACKED="Bounce List Mandelbrot NBody Permute Queens Sieve Storage Towers"
RENAMED="bounce list_tail mandelbrot nbody permute queens sieve storage towers"
PREFIX="are_we_fast_yet"

ARR_RENAMED=($RENAMED)

# TODO: Should we somehow sync all arguments here?
# TODO: we could supply a outer and inner loop iteration count to harness.*

CONFIG_FILE="$(pwd)/../effekt/examples/benchmarks/config_default.txt"

# Hyperfine can only write JSON to files
tmpfile=$(mktemp /tmp/hyperfine_are-we-fast-yet.XXXXX)

# name -> number
get_arg() {
	grep "$PREFIX/$1" "$CONFIG_FILE" | awk '{print $2}'
}

# command -> arg (0 = harness default) -> json
benchmark() {
	if hyperfine --export-json "$tmpfile" "$1" >&2; then
		jq "{mean: .results[0].mean, stddev: .results[0].stddev, arg: ${2:-0}}" "$tmpfile"
	else
		echo "{\"arg\": ${2:-0}}"
	fi
}

# output start
echo "{"

# --- JavaScript benchmarks ---
>&2 echo "::group::js"
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
>&2 echo "::endgroup::"

# --- Python benchmarks ---
>&2 echo "::group::python"
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
>&2 echo "::endgroup::"

# --- Java benchmarks ---
>&2 echo "::group::java"
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
>&2 echo "::endgroup::"

# --- C++ benchmarks ---
>&2 echo "::group::cpp"
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

echo ","
>&2 echo "::endgroup::"

# --- Lexa benchmarks ---
>&2 echo "::group::lexa"
cd ../../../

LEXA_BENCHMARKS="$(pwd)/lexa-benchmarks/$PREFIX"

[ -d lexa ] || git clone https://github.com/lexa-lang/lexa.git >&2
cd lexa/

nix develop .#userShell --accept-flake-config --ignore-environment --keep HOME --command bash -c "
	for benchmark in $RENAMED; do
		./lexa $LEXA_BENCHMARKS/\$benchmark/main.lx -o $LEXA_BENCHMARKS/\$benchmark/main
	done
" >&2

cd "$LEXA_BENCHMARKS"

echo "\"lexa\":"
{
	for benchmark in $RENAMED; do
		echo "{\"$PREFIX/$benchmark\":"
		arg=$(get_arg "$benchmark")
		benchmark "./$benchmark/main $arg" "$arg"
		echo "}"
	done
} | jq -s 'add'

>&2 echo "::endgroup::"

# --- Done ---

# output end
echo "}"
rm "$tmpfile"

>&2 echo "::endgroup::"
