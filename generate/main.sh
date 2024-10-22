#!/bin/env bash
set -e

./build.sh | ./append.sh build

./benchmarks.sh
./cloc.sh | ./append.sh cloc
./out_loc.sh | ./append.sh out_loc
./phases.sh | ./append.sh phases
./metrics.sh | ./append.sh metrics
./backends.sh | ./append.sh backends

# generate index
cd ../data/
find * -type f -name "*.json" -printf "\"%p\"\n" | jq -s . >index.json

# reset effekt repository
git -C ../effekt/ checkout .
