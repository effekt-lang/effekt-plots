package data

import plots.LineStacked
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color

class ByBenchmark(d: js.Array[js.Dynamic]) extends LineStacked {
  override lazy val chartTitle: String = "Accumulated Phase Times per Benchmark"
  override lazy val xLabel = "benchmark date"
  override lazy val yLabel = "time in seconds"

  override def tooltipBody(idx: Int) = js.Array(
    f"Commit: ${d(idx).meta.commit}",
    f"Commit date: ${new js.Date(d(idx).meta.commitDate.asInstanceOf[String].toDouble * 1000).toLocaleString()}"
  )

  lazy val chartData = {
    val keys = js.Object.keys(d(0).total.asInstanceOf[js.Object])
    val colorScheme = Color()

    new ChartData {
      labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
      datasets = keys.map { key =>
        new ChartDataSets {
          label = key
          data = d.map { p => p.total.selectDynamic(key).asInstanceOf[Double] / 1000 }
          backgroundColor = colorScheme.nextColor()
        }
      }
    }
  }
}
