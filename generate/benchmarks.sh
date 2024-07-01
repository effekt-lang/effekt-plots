#!/bin/env bash
set -e

# we need to cd here since some examples import local files
cd ../effekt/

FILES="examples/benchmarks/*.effekt examples/casestudies/*.effekt.md"

# measure build performance first
# for file in $FILES; do
# 	"$(which time)" --verbose effekt.sh -o buildout/ -b "$file" >/dev/null 2>"$file".time.out
# done

# then build and run all with JSON phase timings
# "$(which time)" --verbose effekt.sh -o out/ --time json $FILES >/dev/null 2>../generate/time.out

# now also measure execution time of backends based on benchmark configuration
BACKENDS="llvm js"

for backend in $BACKENDS; do
	log="benchmarks_$backend.log"
	>"$log"
	while read config; do
		arr=($config)
		file="examples/benchmarks/${arr[0]}/${arr[1]}.effekt"
		effekt.sh --backend "$backend" "$file" -- ${arr[2]} >>$log
	done <../generate/benchmark_config.txt
done

# for backend in $BACKENDS; do
# 	for file in $FILES; do
# 		effekt.sh --backend "$backend" -b "$file" &>/dev/null || true
# 		base=$(basename "$file")
# 		path=$(find ./out/ -type f -name "*${base%%.*}") # issue #471
# 		if [ -z "$path" ]; then
# 			echo "$file failed to build for $backend."
# 			continue
# 		fi
# 		"$(which time)" --verbose "$path" >/dev/null 2>"$file".time-"$backend".out || echo "$path failed to run for $backend."
# 	done
# done
