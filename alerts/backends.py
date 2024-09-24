#!/bin/env python3

import statistics
import json
import sys
import os

N = 20
SIGMA_MULT = 3
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
        mean = statistics.mean(runs)
        sd = statistics.stdev(runs)

        newest = merged[backend][file][-1]
        if newest > mean + sd * SIGMA_MULT:
            outliers.append(
                {
                    "backend": backend,
                    "file": file,
                    "value": newest,
                    "mean": mean,
                    "sd": sd,
                }
            )

if outliers:
    print("# Backend outliers detected!\n")
    print(f"Configuration: `N={N}`, significant if `time > μ+{SIGMA_MULT}σ`\n")
    print(
        "\n".join(
            [
                f"- Backend: {outlier['backend']}, file: `{outlier['file']}`, time: {outlier['value'] / 1e9:.3f}s (μ={outlier['mean']/1e9:.3f}s, σ={outlier['sd']/1e9:.3f}s)"
                for outlier in outliers
            ]
        )
    )
    sys.exit(1)
