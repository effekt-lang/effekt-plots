package data

import plots.LineStacked
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color

class PhaseTimes(d: js.Array[js.Dynamic]) extends LineStacked {
  override lazy val chartTitle: String = "Phase Times of Benchmarks"
  override lazy val xLabel = "benchmark date"
  override lazy val yLabel = "time in seconds"

  override def tooltipBody(idx: Int) = js.Array(
    f"Commit: ${d(idx).meta.commit}",
    f"Commit date: ${new js.Date(d(idx).meta.commitDate.asInstanceOf[String].toDouble * 1000).toLocaleString()}"
  )

  lazy val chartData = {
    val keys = js.Object.keys(d(0).asInstanceOf[js.Object])
      .filter { k => k != "meta" && k != "total" }
    val colorScheme = Color()

    new ChartData {
      labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
      datasets = keys.map { key =>
        new ChartDataSets {
          label = key
          data = d.map { p =>
            js.Object.entries(p.selectDynamic(key).asInstanceOf[js.Object]).foldLeft(0.0) {
                case (acc, js.Tuple2(_, s)) => acc + s.asInstanceOf[Double]
            }
          }
          backgroundColor = colorScheme.nextColor()
        }
      }
    }
  }
}