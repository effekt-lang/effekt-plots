#!/bin/env bash
set -e

# we need to cd here since some examples import local files
cd ../effekt/

FILES="examples/casestudies/*.effekt.md"

# measure build performance first
for file in $FILES; do
	"$(which time)" --verbose effekt.sh -o buildout/ -b "$file" >/dev/null 2>"$file".time.out
done

# then build and run all with JSON phase timings
for file in $FILES; do
	effekt.sh -o out/ --time json "$file" &>/dev/null
done

# now also measure execution time of backends based on benchmark configuration
BACKENDS="llvm js"

for backend in $BACKENDS; do
	log="benchmarks_$backend.log"
	>"$log"
	while read config; do
		arr=($config)
		file="examples/benchmarks/${arr[0]}.effekt"
		printf "${arr[0]} ${arr[1]} " >>"$log"
		effekt.sh --backend "$backend" "$file" -- "${arr[1]}" >>"$log"
	done <"examples/benchmarks/config_$backend.txt"
done
