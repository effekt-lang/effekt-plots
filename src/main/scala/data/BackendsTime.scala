package data

// TODO: this should probably be merged with BackendsMemory

import plots.LineStacked
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color
import utils.AnnotationContext

class BackendsTime(d: js.Array[js.Dynamic], backend: String)(implicit C: AnnotationContext) extends LineStacked {
  override def chartTitle: String = s"Execution Time for $backend Backend"
  override def xLabel = "date"
  override def yLabel = "time in seconds"

  override def tooltipBody(idx: Int) = js.Array(
    f"Commit: ${d(idx).meta.commit}",
    f"Commit date: ${new js.Date(d(idx).meta.commitDate.asInstanceOf[String].toDouble * 1000).toLocaleString()}"
  )

  def chartDataOpt = {
    if (d.isEmpty) return None

    val keys = js.Object.keys(d(0).selectDynamic(backend).asInstanceOf[js.Object])
      .filter { k => k != "meta" && k != "total" }

    Some(new ChartData {
      labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
      datasets = keys.map { key =>
        new ChartDataSets {
          label = key
          data = d.map { _.selectDynamic(backend).selectDynamic(key).time.asInstanceOf[Double] / 1e9 }
          backgroundColor = colorScheme.nextColor()
        }
      }
    })
  }
}
