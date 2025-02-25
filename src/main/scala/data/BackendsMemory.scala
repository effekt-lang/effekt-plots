package data

// TODO: this should probably be merged with BackendsTime

import plots.LineStacked
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color
import utils.AnnotationContext

class BackendsMemory(d: js.Array[js.Dynamic], backend: String)(implicit C: AnnotationContext) extends LineStacked {
  override def chartTitle: String = s"Maximum Memory for $backend Backend"
  override def xLabel = "date"
  override def yLabel = "memory usage in kilobyte"

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

    Some(new ChartData {
      labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
      datasets = keys.map { key =>
        new ChartDataSets {
          label = key
          data = d.map { p =>
            if (p.asInstanceOf[js.Object].hasOwnProperty(backend)
              && p.selectDynamic(backend).asInstanceOf[js.Object].hasOwnProperty(key)) {
              val entry = p.selectDynamic(backend).selectDynamic(key)
              if (entry.asInstanceOf[js.Object].hasOwnProperty("maxMem"))
                entry.maxMem.asInstanceOf[Double] / 1e9
              else 0
            }
            else 0
          }
          backgroundColor = colorScheme.nextColor()
        }
      }
    })
  }
}
