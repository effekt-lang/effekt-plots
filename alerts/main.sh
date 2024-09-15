#!/bin/sh

backend_outliers=$(./backends.py)

if [ $? = 1 ]; then
	gh issue comment 5 -b "$backend_outliers" -R "effekt-lang/effekt-plots"
fi
