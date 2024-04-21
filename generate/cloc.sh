#!/bin/env bash

cd ../effekt/
commit="$(git rev-parse --short HEAD)"
cloc effekt/ libraries/ examples/ --json | jq --arg commit "$commit" 'del(.header) + {meta: {commit: $commit}}'
