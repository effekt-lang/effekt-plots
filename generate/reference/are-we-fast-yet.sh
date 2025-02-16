#!/bin/env bash
set -e

RUNS=5

# JavaScript benchmarks

cd are-we-fast-yet/benchmarks/JavaScript/

TRACKED="Bounce List Mandelbrot NBody Permute Queens Sieve Storage Towers"
RENAMED="bounce list_tail mandelbrot nbody permute queens sieve storage towers"
PREFIX="are_we_fast_yet"

log="../../../../../effekt/benchmarks_js_reference.log"
>"$log"

i=0
for benchmark in $TRACKED; do
	total_time=0
	total_mem=0
	for run in $(seq $RUNS); do
		read -r time mem <<<$("$(which time)" --verbose node harness.js "$benchmark" 2>&1 |
			gawk 'NR==6{time=split($0, arr1, " ")}; match($0, /.*Maximum resident set size \(kbytes\): ([0-9]+)/, arr2){print arr1[3], arr2[1]}')

		time=${time::-2} # remove us suffix
		total_time=$((total_time + time * 1000))
		total_mem=$((total_mem + mem))
	done

	arr=($RENAMED)
	renamed=${arr[$i]}

	average_time=$((total_time / RUNS))
	average_mem=$((total_mem / RUNS))
	echo "$PREFIX/$renamed: 0 $average_time $average_mem" >>"$log"

	i=$((i + 1))
done

cd ../../../
