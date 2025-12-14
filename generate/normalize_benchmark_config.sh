#!/bin/env bash
set +e

echo "WARNING: This normalization is very basic and may not always find a fitting argument." >&2

BACKEND="llvm"
ARG_INIT=2
TIMEOUT=10          # in seconds
GOAL_MIN=1000000000 # in nanoseconds
GOAL_MAX=1500000000 # in nanoseconds

cd ../effekt

while read config; do
	arr=($config)
	file="examples/benchmarks/${arr[0]}/${arr[1]}.effekt"
	echo "${arr[0]}/${arr[1]}" >&2

	arg=$ARG_INIT
	while true; do
		time="$(timeout "$TIMEOUT"s effekt --backend "$BACKEND" "$file" -- "$arg")"
		if [[ "$?" -eq 124 ]]; then
			echo "  $arg timeouted" >&2
			arg=$((arg - (arg / 3) - 1))
			continue
		fi

		echo "  $arg took $time" >&2

		if [[ "$time" -lt "$GOAL_MIN" ]]; then
			arg=$((arg * 2))
			continue
		fi
		if [[ "$time" -gt "$GOAL_MIN" ]] && [[ "$time" -lt "$GOAL_MAX" ]]; then
			echo "${arr[0]} ${arr[1]} $arg"
			break
		fi
		if [[ "$time" -gt "$GOAL_MAX" ]]; then
			arg=$((arg - (arg / 3)))
			continue
		fi
	done
done <examples/benchmarks/config_"$BACKEND".txt
