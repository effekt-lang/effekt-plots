#!/bin/env bash
set -e

./benchmarks.sh
./build.sh | ./append.sh build.json
./cloc.sh | ./append.sh cloc.json
./phases.sh | ./append.sh phases.json
./metrics.sh | ./append.sh metrics.json
