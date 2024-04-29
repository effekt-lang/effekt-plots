package data

import plots.Line
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color

class MemoryUsage(d: js.Array[js.Dynamic]) extends Line {
  override lazy val chartTitle: String = "Memory Usage of Benchmarks"
  override lazy val xLabel = "benchmark date"
  override lazy val yLabel = "memory usage in kilobyte"

  lazy val chartData = {
    new ChartData {
      labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
      datasets = js.Array(
        new ChartDataSets {
          label = "memory usage (kB)"
          data = d.map { s => s.maxMem.asInstanceOf[String].toInt }
          fill = false
          backgroundColor = colorScheme.nextColor()
          borderColor = backgroundColor
        }
      )
    }
  }
}
