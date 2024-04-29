#!/bin/env bash
set -e

cd ../effekt/
cloc effekt/ libraries/ examples/ kiama/ --json --read-lang-def ../generate/cloc.cfg | jq 'del(.header)'
