package data

import plots.Line
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.randomColor

class CpuUsage(d: js.Array[js.Dynamic]) extends Line {
  override lazy val chartTitle: String = "CPU Usage for all Benchmarks"
  override lazy val xLabel = "commit hash"
  override lazy val yLabel = "cpu usage"

  lazy val chartData = {
    new ChartData {
      labels = d.map { _.meta.commit.asInstanceOf[String] }
      datasets = js.Array(
        new ChartDataSets {
          label = "cpu usage (%)"
          data = d.map { s => s.cpuUsage.asInstanceOf[String].init.toInt }
          backgroundColor = randomColor()
        }
      )
    }
  }
}
