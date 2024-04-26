package data

import plots.Line
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.randomColor

class TimeMeasure(d: js.Array[js.Dynamic]) extends Line {
  override lazy val chartTitle: String = "Accumulated Time of Benchmarks"
  override lazy val xLabel = "commit hash"
  override lazy val yLabel = "time in seconds"

  lazy val chartData = {
    new ChartData {
      labels = d.map { _.meta.commit.asInstanceOf[String] }
      datasets = js.Array(
        new ChartDataSets {
          label = "time (s)"
          data = d.map { s => s.userTime.asInstanceOf[String].toDouble }
          backgroundColor = randomColor()
        }
      )
    }
  }
}
