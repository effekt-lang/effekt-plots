# Effekt Plots

Runs benchmarks of the [Effekt programming language](https://effekt-lang.org) and visualizes various metrics with plots.

## Usage

0. Clone repository recursively and (if needed) sync `effekt/` to newest commit
1. Install `sbt`
3. Generate data (also installs Effekt): `cd generate; ./main.sh; cd ..`
3. Compile ScalaJS to JavaScript: `sbt fastOptJS::webpack`
4. Open `index.html` in a browser