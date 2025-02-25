#!/bin/env bash
set -e

>&2 echo "::group::$0"

./build.sh | ./append.sh build
./reference.sh | ./append.sh reference
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

>&2 echo "::endgroup::"
