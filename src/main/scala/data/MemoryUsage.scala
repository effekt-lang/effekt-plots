package data

import plots.Line
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.randomColor

class MemoryUsage(d: js.Array[js.Dynamic]) extends Line {
  override lazy val chartTitle: String = "Memory Usage for all Benchmarks"
  override lazy val xLabel = "commit hash"
  override lazy val yLabel = "memory usage"

  lazy val chartData = {
    new ChartData {
      labels = d.map { _.meta.commit.asInstanceOf[String] }
      datasets = js.Array(
        new ChartDataSets {
          label = "memory usage (kB)"
          data = d.map { s => s.maxMem.asInstanceOf[String].toInt }
          backgroundColor = randomColor()
        }
      )
    }
  }
}
