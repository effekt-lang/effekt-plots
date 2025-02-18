package data

import plots.Bar
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color
import utils.AnnotationContext

class Reference(benchmark: String, d: js.Dynamic)(implicit C: AnnotationContext) extends Bar {
  override def chartTitle: String = "Time for Building Effekt"
  override def xLabel = "date"
  override def yLabel = "time in seconds"

  override def tooltipBody(idx: Int) =
    js.Array(
      f"Commit: ${d(idx).meta.commit}",
      f"Commit date: ${new js.Date(d(idx).meta.commitDate.asInstanceOf[String].toDouble * 1000).toLocaleString()}"
    )

  def chartDataOpt = {
    None
    // Some(new ChartData {
    //   labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
    //   datasets = js.Array(
    //     new ChartDataSets {
    //       label = "time (s)"
    //       data = d.map { s => s.buildTime.asInstanceOf[String].toDouble }
    //       fill = false
    //       backgroundColor = colorScheme.nextColor()
    //       borderColor = backgroundColor
    //     }
    //   )
    // })
  }
}
