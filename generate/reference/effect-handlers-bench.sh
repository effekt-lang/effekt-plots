#!/bin/env bash
set -e

>&2 echo "::group::$0"

TRACKED="countdown iterator nqueens parsing_dollars product_early resume_nontail tree_explore triples"
PREFIX="effect_handlers_bench"

CONFIG_FILE="../../../../../effekt/examples/benchmarks/config_default.txt"

# Hyperfine can only write JSON to files
tmpfile=$(mktemp /tmp/hyperfine_effect-handlers-bench.XXXXX)

# name -> number
get_arg() {
	grep "$PREFIX/$1" "$CONFIG_FILE" | awk '{print $2}'
}

# command -> arg -> json
benchmark() {
	if hyperfine --export-json "$tmpfile" "$1" >&2; then
		jq "{mean: .results[0].mean, stddev: .results[0].stddev, arg: $2}" "$tmpfile"
	else
		echo "{\"arg\": $2}"
	fi
}

# output start
echo "{"

# --- Ocaml benchmarks ---
>&2 echo "::group::ocaml"
cd reference/effect-handlers-bench/benchmarks/ocaml/

eval $(opam env --switch=4.12.0+domains+effects --set-switch)

make build >&2

echo "\"ocaml\":"
{
	for benchmark in $TRACKED; do
		echo "{\"$PREFIX/$benchmark\":"
		arg=$(get_arg "$benchmark")
		benchmark "./$benchmark/main $arg" "$arg"
		echo "}"
	done
} | jq -s 'add'

echo ","
>&2 echo "::endgroup::"

# --- Eff benchmarks ---
>&2 echo "::group::eff"
cd ../eff/

eval $(opam env --switch=/tmp/runner/effekt-plots/effekt-plots --set-switch)

make build >&2

echo "\"eff\":"
{
	for benchmark in $TRACKED; do
		echo "{\"$PREFIX/$benchmark\":"
		arg=$(get_arg "$benchmark")
		benchmark "./$benchmark/main $arg" "$arg"
		echo "}"
	done
} | jq -s 'add'

>&2 echo "::endgroup::"

# --- Koka benchmarks ---

# TODO: This uses a very old version and is broken with current

# --- Done ---

# output end
echo "}"
rm "$tmpfile"

>&2 echo "::endgroup::"
