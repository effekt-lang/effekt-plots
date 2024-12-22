#!/bin/env bash
set -e
set -x

cd ../effekt/
git submodule update --init --recursive

sbt install >/dev/null
echo "DONE"

time=$({ $(which time) -f '%e' sbt install >/dev/null; } 2>&1 | tail -n1)
jq -n \
	--arg buildTime "$time" \
	'{buildTime: $buildTime}'
