#!/bin/env python3

import statistics
import json
import sys
import os

N = 10
Z_THRESHOLD = 2
DIR = "../data/backends/"

files = sorted(os.listdir(DIR))
data = [o for f in files for o in json.load(open(f"{DIR}/{f}"))]

data = data[-(N + 1) :]


def merge(key):
    out = {}
    for obj in data:
        for i, d in obj[key].items():
            if i not in out:
                out[i] = []
            out[i].append(d["time"])
    return out


backends = [*data[0].keys()]
backends.remove("meta")

merged = {backend: merge(backend) for backend in backends}

outliers = []
for backend in backends:
    for file in merged[backend]:
        runs = merged[backend][file][:-1]
        if len(runs) < 5:
            continue

        mean = statistics.mean(runs)
        sd = statistics.stdev(runs)

        newest = merged[backend][file][-1]
        z = (newest - mean) / sd
        if abs(z) > Z_THRESHOLD:
            outliers.append(
                {
                    "backend": backend,
                    "file": file,
                    "value": newest,
                    "mean": mean,
                    "sd": sd,
                    "z": z,
                }
            )


def emoji(z):
    SIGNIFICANT_MULT = 1.5
    if z > 0:
        if z > SIGNIFICANT_MULT * Z_THRESHOLD:
            return "⏫"
        else:
            return "🔼"
    else:
        if z < -SIGNIFICANT_MULT * Z_THRESHOLD:
            return "⏬"
        else:
            return "🔽"


if outliers:
    print("# Backend outliers detected!\n")
    print(f"Configuration: `N={N}`, significant if `z-score > {Z_THRESHOLD}`\n")
    print(
        "\n".join(
            [
                f"- {emoji(outlier['z'])} in backend: {outlier['backend']}, file: `{outlier['file']}`, time: {outlier['value'] / 1e9:.3f}s (μ={outlier['mean']/1e9:.3f}s, σ={outlier['sd']/1e9:.3f}s, z={outlier['z']:.3f})"
                for outlier in outliers
            ]
        )
    )
    sys.exit(1)
