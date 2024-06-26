#!/bin/env bash
set -e

cd ../effekt/
cloc $(git ls-files --recurse-submodules) --json --read-lang-def ../generate/cloc.cfg | jq 'del(.header)'
