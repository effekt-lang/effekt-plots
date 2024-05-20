#!/bin/env bash
set -e

combineMetrics() {
	TIMEFILE="$1"

	maxMem=$(grep "Maximum resident set size" "$TIMEFILE" | awk '{print $6}')
	userTime=$(grep "User time" "$TIMEFILE" | awk '{print $4}')
	cpuUsage=$(grep "Percent of CPU this job got" "$TIMEFILE" | awk '{print $7}')

	jq -n \
		--arg maxMem "$maxMem" \
		--arg userTime "$userTime" \
		--arg cpuUsage "$cpuUsage" \
		'{maxMem: $maxMem, userTime: $userTime, cpuUsage: $cpuUsage}'

	rm "$TIMEFILE"
}

{
	for file in $(find ../effekt/ -name "*.time.out"); do
		_file="${file//"../effekt/"/}"
		_file="${_file//".time.out"/}"
		echo "{\"$_file\":"
		combineMetrics "$file"
		echo "}"
	done
	echo "{\"total\":"
	combineMetrics "time.out"
	echo "}"
} | jq -s 'add'
