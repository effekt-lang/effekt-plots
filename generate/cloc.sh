#!/bin/env bash
set -e

cd ../effekt/
cloc effekt/ libraries/ examples/ --json | jq 'del(.header)'
