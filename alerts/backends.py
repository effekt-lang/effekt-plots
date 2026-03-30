#!/usr/bin/env python3

import statistics
import json
import sys
import os

N = 10
MODIFIED_Z_THRESHOLD = 10 # ~ 7 sigma
DIR = "../data/backends/"
EPSILON = sys.float_info.epsilon

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

        newest = merged[backend][file][-1]

        mean = statistics.mean(runs)
        sd = statistics.stdev(runs)
        # uses Median Absolute Deviation to (try to) be robust to historical outliers.
        med = statistics.median(runs)
        mad = statistics.median([abs(x - med) for x in runs])
        if mad == 0:
            # all values are identical ~> any deviation is significant
            mad = EPSILON

        mz = (newest - med) / mad

        if abs(mz) > MODIFIED_Z_THRESHOLD:
            outliers.append(
                {
                    "backend": backend,
                    "file": file,
                    "value": newest,
                    "mean": mean,
                    "sd": sd,
                    "median": med,
                    "mad": mad,
                    "mz": mz,
                }
            )


def emoji(mz):
    SIGNIFICANT_MULT = 1.333
    if mz > 0:
        if mz > SIGNIFICANT_MULT * MODIFIED_Z_THRESHOLD:
            return "⏫"
        else:
            return "🔼"
    else:
        if mz < -SIGNIFICANT_MULT * MODIFIED_Z_THRESHOLD:
            return "⏬"
        else:
            return "🔽"


if outliers:
    print("# Backend outliers detected!\n")
    print(f"Configuration: `N={N}`, significant if `modified z-score > {MODIFIED_Z_THRESHOLD:.2f}`\n")
    print(
        "\n".join(
            [
                f"- {emoji(outlier['mz'])} in backend: {outlier['backend']}, file: `{outlier['file']}`, time: {outlier['value'] / 1e9:.3f}s (μ={outlier['mean']/1e9:.3f}s, σ={outlier['sd']/1e9:.3f}s, median={outlier['median']/1e9:.3f}s, MAD={outlier['mad']/1e9:.4f}s, mz={outlier['mz']:.2f})"
                for outlier in outliers
            ]
        )
    )
    sys.exit(1)
