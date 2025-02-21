package data

import plots.Bar
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color
import utils.AnnotationContext
import js.JSConverters._

class Reference(benchmark: String, d: js.Object)(implicit C: AnnotationContext) extends Bar {
  override def chartTitle: String = "Effekt vs. Reference: " + benchmark
  override def xLabel = "language"
  override def yLabel = "time in seconds"

  override def tooltipBody(idx: Int) = {
    val languages = filterLanguages()
    val obj = d.asInstanceOf[js.Dynamic].selectDynamic(languages(idx)).selectDynamic(benchmark)
    js.Array(
      f"Arg: ${obj.arg}",
      f"Stddev: ${obj.stddev}"
    )
  }

  def filterLanguages() =
    js.Object.keys(d).filter { language =>
      d.asInstanceOf[js.Dynamic].selectDynamic(language).asInstanceOf[js.Object]
        .hasOwnProperty(benchmark) && language != "meta"
    }.map(_.toString)

  def chartDataOpt = {
    val languages = filterLanguages()

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
            else 0 // overflow, segfault etc.
          }
          backgroundColor = colorScheme.scheme.toJSArray
        }
      )
    })
  }
}
