package data

import plots.Bar
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.{AnnotationContext, Fields, Json, ReferenceMetric}

class Reference(benchmark: String, referenceResults: Fields[Fields[ReferenceMetric]])(implicit annotationContext: AnnotationContext) extends Bar {
  override def chartTitle: String = "Effekt vs. Reference: " + benchmark
  override def xLabel = "language"
  override def yLabel = "time in seconds (normalized)"

  private case class LanguageResult(language: String, measurement: ReferenceMetric)

  private lazy val measurements = Json.keys(referenceResults)
    .filter(language => language != "meta" && referenceResults(language).contains(benchmark))
    .map(language => LanguageResult(language, referenceResults(language)(benchmark)))

  override def tooltipBody(index: Int) = {
    val measurement = measurements(index).measurement
    js.Array(
      s"Arg: ${measurement.arg}",
      s"Mean: ${measurement.mean.toOption.map(_.toString).getOrElse("n/a")}",
      s"Stddev: ${measurement.stddev.toOption.map(_.toString).getOrElse("n/a")}"
    )
  }

  def chartDataOpt = {
    if (measurements.isEmpty) return None

    val normalizationValue = js.Array("effekt_llvm", "effekt_js")
      .flatMap(language => referenceResults.get(language).flatMap(_.get(benchmark)).flatMap(_.mean.toOption).toList)
      .headOption.getOrElse(1.0)
    val normalizedValues = measurements.map(_.measurement.mean.toOption.map(_ / normalizationValue))
    val highestValue = normalizedValues.collect { case Some(value) => value }.maxOption.getOrElse(0.0)

    Some(new ChartData {
      labels = measurements.map(_.language)
      datasets = js.Array(new ChartDataSets {
        data = normalizedValues.map(_.getOrElse(highestValue))
        backgroundColor = normalizedValues.map(_.fold("rgba(0, 0, 0, 0.1)")(_ => colorScheme.nextColor()))
      })
    })
  }
}
