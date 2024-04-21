#!/bin/env bash
set -e

# run benchmarks and gather as much information as possible
$(which time) --verbose effekt.sh --time json ../effekt/examples/benchmarks/*.effekt >/dev/null 2>time.out
