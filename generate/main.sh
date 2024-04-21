#!/bin/env bash
set -e

./benchmarks.sh
./cloc.sh | ./append.sh cloc.json
./phases.sh | ./append.sh phases.json
./metrics.sh | ./append.sh metrics.json
