package data

import plots.LineStacked
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color
import utils.AnnotationContext

class BackendDiff(d: js.Array[js.Dynamic], backend1: String, backend2: String)(implicit C: AnnotationContext) extends LineStacked {
  override def chartTitle: String = s"Difference between execution time of backends ($backend1-$backend2)"
  override def xLabel = "date"
  override def yLabel = "time in seconds"

  override def tooltipBody(idx: Int) = js.Array(
    f"Commit: ${d(idx).meta.commit}",
    f"Commit date: ${new js.Date(d(idx).meta.commitDate.asInstanceOf[String].toDouble * 1000).toLocaleString()}"
  )

  def chartData = {
    val keys = js.Object.keys(d(0).selectDynamic(backend1).asInstanceOf[js.Object])
      .filter { k => k != "meta" && k != "total" }

    new ChartData {
      labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
      datasets = keys.map { key =>
        new ChartDataSets {
          label = key
          data = d.map { e =>
            (e.selectDynamic(backend1).selectDynamic(key).time.asInstanceOf[Double] -
            e.selectDynamic(backend2).selectDynamic(key).time.asInstanceOf[Double]) / 1e9
          }
          backgroundColor = colorScheme.nextColor()
        }
      }
    }
  }
}
