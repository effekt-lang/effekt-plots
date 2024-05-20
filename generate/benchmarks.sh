#!/bin/env bash
set -e

# we need to cd here since some examples import local files
cd ../effekt/

files="examples/benchmarks/*.effekt examples/casestudies/*.effekt.md"

# build all first
for file in $files; do
	"$(which time)" --verbose effekt.sh -o buildout/ -b "$file" >/dev/null 2>"$file".time.out
done

# then run completely with JSON phase timings
"$(which time)" --verbose effekt.sh -o out/ --time json $files >/dev/null 2>../generate/time.out
