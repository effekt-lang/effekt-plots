package data

import plots.LineStacked
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color
import utils.AnnotationContext

class GeneratedCodeSize(d: js.Array[js.Dynamic], backend: String)(implicit C: AnnotationContext) extends LineStacked {
  override def chartTitle: String = "Generated Code Size of Benchmarks"
  override def xLabel = "date"
  override def yLabel = "lines of code"

  override def tooltipBody(idx: Int) = js.Array(
    f"Commit: ${d(idx).meta.commit}",
    f"Commit date: ${new js.Date(d(idx).meta.commitDate.asInstanceOf[String].toDouble * 1000).toLocaleString()}"
  )

  def chartDataOpt = {
    if (d.isEmpty) return None

    // TODO: We should really use actual structs / JSON
    val main = d.asInstanceOf[js.Array[js.Object]].reverse.find(_.hasOwnProperty(backend))
    if (main.isEmpty) return None

    val keys = js.Object.keys(main.get.asInstanceOf[js.Dynamic].selectDynamic(backend).asInstanceOf[js.Object])
      .filter { k => k != "meta" && k != "SUM" }

    Some(new ChartData {
      labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
      datasets = keys.map { key =>
        new ChartDataSets {
          label = key
          data = d.map { p =>
            if (p.asInstanceOf[js.Object].hasOwnProperty(backend)
              && p.selectDynamic(backend).asInstanceOf[js.Object].hasOwnProperty(key))
              p.selectDynamic(backend).selectDynamic(key).code.asInstanceOf[Double]
            else 0
          }
          backgroundColor = colorScheme.nextColor()
        }
      }
    })
  }
}
