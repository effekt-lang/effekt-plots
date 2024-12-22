#!/bin/env bash
set -e

>&2 echo "$0"

# we need to cd here since some examples import local files
cd ../effekt/

# selective benchmarks for build and phase timings
FILES="examples/casestudies/*.effekt.md"

# measure build performance first
for file in $FILES; do
	"$(which time)" --verbose effekt.sh -o buildout/ -b "$file" >/dev/null 2>"$file.time.out"
done

# then build and run all with JSON phase timings
for file in $FILES; do
	effekt.sh -o out/ --time json "$file" &>/dev/null
done

# now also measure execution time of backends based on benchmark configuration
RUNS=5
BACKENDS="llvm js"

for backend in $BACKENDS; do
	log="benchmarks_$backend.log"
	>"$log"
	while read config; do
		arr=($config)
		file="examples/benchmarks/${arr[0]}.effekt"
		outfile="out/$(basename "$file" .effekt)"
		printf "${arr[0]} ${arr[1]} " >>"$log"

		effekt.sh --backend "$backend" -b "$file"

		total_time=0
		total_mem=0
		for run in $(seq $RUNS); do
			# gnutime only measures maximum memory here!
			# the actual time measurement is already calculated within the benchmarks themself
			read -r time mem <<<$("$(which time)" --verbose ./"$outfile" "${arr[1]}" 2>&1 |
				gawk 'NR==1{time=$0}; match($0, /.*Maximum resident set size \(kbytes\): ([0-9]+)/, arr){print time, arr[1]}')

			total_time=$((total_time + time))
			total_mem=$((total_mem + mem))
		done

		average_time=$((total_time / RUNS))
		average_mem=$((total_mem / RUNS))
		echo "$average_time $average_mem" >>"$log"
	done <"examples/benchmarks/config_$backend.txt"
done
