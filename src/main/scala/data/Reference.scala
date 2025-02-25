package data

import plots.Bar
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color
import utils.AnnotationContext
import js.JSConverters._

class Reference(benchmark: String, d: js.Dynamic)(implicit C: AnnotationContext) extends Bar {
  override def chartTitle: String = "Effekt vs. Reference: " + benchmark
  override def xLabel = "language"
  override def yLabel = "time in seconds (normalized)"

  override def tooltipBody(idx: Int) = {
    val languages = filterLanguages()
    val obj = d.selectDynamic(languages(idx)).selectDynamic(benchmark)
    js.Array(
      f"Arg: ${obj.arg}",
      f"Mean: ${obj.mean}",
      f"Stddev: ${obj.stddev}"
    )
  }

  def filterLanguages() =
    js.Object.keys(d.asInstanceOf[js.Object]).filter { language =>
      d.selectDynamic(language).asInstanceOf[js.Object]
        .hasOwnProperty(benchmark) && language != "meta"
    }.map(_.toString)

  def getLanguage(language: String) =
    d.selectDynamic(language).selectDynamic(benchmark)

  def chartDataOpt = {
    val normalizationOrder = Array("effekt_llvm", "effekt_js")

    val languages = filterLanguages()

    if (languages.isEmpty) return None

    val normalizationValue = normalizationOrder.map(getLanguage).filter { obj =>
      obj.asInstanceOf[js.Object].hasOwnProperty("mean")
    }.headOption.map(_.mean.asInstanceOf[Double]).getOrElse(1.0)

    val successfulRuns = languages.map(getLanguage).map {
      _.asInstanceOf[js.Object].hasOwnProperty("mean")
    }
    val maxValue = successfulRuns.zip(languages).filter(_._1)
      .map((_, language) => getLanguage(language).mean.asInstanceOf[Double])
      .max * (1.0 / normalizationValue)

    Some(new ChartData {
      labels = languages.map { l => l }
      datasets = js.Array(
        new ChartDataSets {
          data = successfulRuns.zip(languages).map {
            case (true, language) =>
              (1.0 / normalizationValue) * getLanguage(language).mean.asInstanceOf[Double]
            case (false, _) => maxValue
          }
          backgroundColor = successfulRuns.zip(colorScheme.scheme.toJSArray).map {
            case (true, color) => color
            case (false, color) => "rgba(0, 0, 0, 0.1)"
          }
        }
      )
    })
  }
}
