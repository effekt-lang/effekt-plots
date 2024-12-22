#!/bin/env bash
set -e

>&2 echo "$0"

cd ../effekt/
cloc $(git ls-files --recurse-submodules) --json --read-lang-def ../generate/cloc.cfg | jq 'del(.header)'
