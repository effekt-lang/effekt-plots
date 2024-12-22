#!/bin/env bash
set -e

>&2 echo "$0"

cd ../effekt/
git submodule update --init --recursive

time=$({ $(which time) -f '%e' bash -c "sbt install &>/dev/null"; } 2>&1 | tail -n1)
jq -n \
	--arg buildTime "$time" \
	'{buildTime: $buildTime}'
