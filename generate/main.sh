#!/bin/env bash
set -e

./build.sh | ./append.sh build.json

./benchmarks.sh
./cloc.sh | ./append.sh cloc.json
./phases.sh | ./append.sh phases.json
./metrics.sh | ./append.sh metrics.json
