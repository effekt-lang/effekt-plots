package data

import plots.Line
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.randomColor

class CpuUsage(d: js.Array[js.Dynamic]) extends Line {
  override lazy val chartTitle: String = "CPU Usage of Benchmarks"
  override lazy val xLabel = "benchmark date"
  override lazy val yLabel = "CPU usage in percent"

  lazy val chartData = {
    new ChartData {
      labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
      datasets = js.Array(
        new ChartDataSets {
          label = "CPU usage (%)"
          data = d.map { s => s.cpuUsage.asInstanceOf[String].init.toInt }
          backgroundColor = randomColor()
        }
      )
    }
  }
}
