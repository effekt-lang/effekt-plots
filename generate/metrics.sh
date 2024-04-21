#!/bin/env bash
set -e

maxMem=$(grep "Maximum resident set size" time.out | awk '{print $6}')
userTime=$(grep "User time" time.out | awk '{print $4}')
cpuUsage=$(grep "Percent of CPU this job got" time.out | awk '{print $7}')

jq -n \
	--arg maxMem "$maxMem" \
	--arg userTime "$userTime" \
	--arg cpuUsage "$cpuUsage" \
	'{maxMem: $maxMem, userTime: $userTime, cpuUsage: $cpuUsage}'

rm time.out
