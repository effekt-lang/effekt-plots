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

  def chartData = {
    val keys = js.Object.keys(d(0).selectDynamic(backend).asInstanceOf[js.Object])
      .filter { k => k != "meta" && k != "SUM" }

    new ChartData {
      labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
      datasets = keys.map { key =>
        new ChartDataSets {
          label = key
          data = d.map { e =>
            val value = e.selectDynamic(backend).selectDynamic(key)
            if (value == js.undefined) 0
            else value.code.asInstanceOf[Double]
          }
          backgroundColor = colorScheme.nextColor()
        }
      }
    }
  }
}
