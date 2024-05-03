package data

import plots.Line
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color

import org.scalajs.dom

class BuildTime(d: js.Array[js.Dynamic]) extends Line {
  override lazy val chartTitle: String = "Time for Building Effekt"
  override lazy val xLabel = "benchmark date"
  override lazy val yLabel = "time in seconds"

  override def tooltipBody(idx: Int) =
    dom.console.log(d)
    dom.console.log(idx)
    js.Array(
      f"Commit: ${d(idx).meta.commit}",
      f"Commit date: ${new js.Date(d(idx).meta.commitDate.asInstanceOf[String].toDouble * 1000).toLocaleString()}"
    )

  lazy val chartData = {
    new ChartData {
      labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
      datasets = js.Array(
        new ChartDataSets {
          label = "time (s)"
          data = d.map { s => s.buildTime.asInstanceOf[String].toDouble }
          fill = false
          backgroundColor = colorScheme.nextColor()
          borderColor = backgroundColor
        }
      )
    }
  }
}
