#!/bin/env bash
set -e

# we need to cd here since some examples import local files
cd ../effekt/

files="examples/benchmarks/*.effekt examples/casestudies/*.effekt.*"

# run benchmarks and gather as much information as possible
$(which time) --verbose effekt.sh --time json $files >/dev/null 2>../generate/time.out
