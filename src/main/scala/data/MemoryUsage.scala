package data

import plots.LineStacked
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color

class MemoryUsage(d: js.Array[js.Dynamic]) extends LineStacked {
  override lazy val chartTitle: String = "Memory Usage of Benchmarks"
  override lazy val xLabel = "benchmark date"
  override lazy val yLabel = "memory usage in kilobyte"

  override def tooltipBody(idx: Int) = js.Array(
    f"Commit: ${d(idx).meta.commit}",
    f"Commit date: ${new js.Date(d(idx).meta.commitDate.asInstanceOf[String].toDouble * 1000).toLocaleString()}"
  )

  lazy val chartData = {
    val keys = js.Object.keys(d(0).asInstanceOf[js.Object])
      .filter { k => k != "meta" && k != "total" }

    new ChartData {
      labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
      datasets = keys.map { key =>
        new ChartDataSets {
          label = key
          data = d.map { _.selectDynamic(key).maxMem.asInstanceOf[String].toInt }
          backgroundColor = colorScheme.nextColor()
        }
      }
    }
  }
}
