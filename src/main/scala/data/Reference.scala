package data

import plots.Bar
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color
import utils.AnnotationContext

class Reference(benchmark: String, d: js.Object)(implicit C: AnnotationContext) extends Bar {
  override def chartTitle: String = "Effekt vs. Reference Implementation: " + benchmark
  override def xLabel = "language"
  override def yLabel = "time in seconds"

  // override def tooltipBody(idx: Int) =
  //   js.Array(
  //     f"Commit: ${d(idx).meta.commit}",
  //     f"Commit date: ${new js.Date(d(idx).meta.commitDate.asInstanceOf[String].toDouble * 1000).toLocaleString()}"
  //   )

  def chartDataOpt = {
    val languages = js.Object.keys(d).filter { language =>
      d.asInstanceOf[js.Dynamic].selectDynamic(language).asInstanceOf[js.Object]
        .hasOwnProperty(benchmark) && language != "meta"
    }.map(_.toString)

    if (languages.isEmpty) return None

    Some(new ChartData {
      labels = languages.map { l => l }
      datasets = js.Array(
        new ChartDataSets {
          label = "time (s)"
          data = languages.map { language =>
            val obj = d.asInstanceOf[js.Dynamic].selectDynamic(language).selectDynamic(benchmark)
            if (obj.asInstanceOf[js.Object].hasOwnProperty("mean"))
              obj.mean.asInstanceOf[Double]
            else 0
          }
          fill = false
          backgroundColor = colorScheme.nextColor()
          borderColor = backgroundColor
        }
      )
    })
  }
}
