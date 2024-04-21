#!/bin/env bash

DATA=../data

jq '. += [input]' "$DATA"/"$1" - <&0 >"$1".temp
mv "$1.temp" "$DATA"/"$1" # sponge
