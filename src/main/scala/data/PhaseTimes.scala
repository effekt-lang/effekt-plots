package data

import plots.LineStacked
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color
import utils.AnnotationContext

class PhaseTimes(d: js.Array[js.Dynamic])(implicit C: AnnotationContext) extends LineStacked {
  override def chartTitle: String = "Phase Times of Programs"
  override def xLabel = "date"
  override def yLabel = "time in seconds"

  override def tooltipBody(idx: Int) = js.Array(
    f"Commit: ${d(idx).meta.commit}",
    f"Commit date: ${new js.Date(d(idx).meta.commitDate.asInstanceOf[String].toDouble * 1000).toLocaleString()}"
  )

  def chartDataOpt = {
    if (d.isEmpty) return None

    val keys = js.Object.keys(d(0).asInstanceOf[js.Object])
      .filter { k => k != "meta" && k != "total" }

    Some(new ChartData {
      labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
      datasets = keys.map { key =>
        new ChartDataSets {
          label = key
          data = d.map { p =>
            // TODO: the property check should be done for all folded data (abstracted to a class?)
            if (p.asInstanceOf[js.Object].hasOwnProperty(key))
              js.Object.entries(p.selectDynamic(key).asInstanceOf[js.Object]).foldLeft(0.0) {
                case (acc, js.Tuple2(_, s)) => acc + (s.asInstanceOf[Double] / 1000)
              }
            else 0
          }
          backgroundColor = colorScheme.nextColor()
        }
      }
    })
  }
}
