package data

import plots.Line
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color

class TimeMeasure(d: js.Array[js.Dynamic]) extends Line {
  override lazy val chartTitle: String = "Accumulated Time of Benchmarks"
  override lazy val xLabel = "benchmark date"
  override lazy val yLabel = "time in seconds"

  lazy val chartData = {
    new ChartData {
      labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
      datasets = js.Array(
        new ChartDataSets {
          label = "time (s)"
          data = d.map { s => s.userTime.asInstanceOf[String].toDouble }
          fill = false
          backgroundColor = colorScheme.nextColor()
          borderColor = backgroundColor
        }
      )
    }
  }
}
