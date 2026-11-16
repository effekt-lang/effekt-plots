package data

import plots.TimedLineStacked
import scala.scalajs.js
import utils.{AnnotationContext, Fields, Json, SizeMetric, TimedPoint}

class GeneratedCodeSize(val dataPoints: js.Array[TimedPoint], backend: String)(implicit annotationContext: AnnotationContext) extends TimedLineStacked {
  override def chartTitle: String = "Generated Code Size of Benchmarks"
  override def xLabel = "date"
  override def yLabel = "lines of code"
  override def points = dataPoints

  def chartDataOpt = seriesData(
    points = dataPoints,
    seriesAt = point => Json.fields[Fields[SizeMetric]](point).getOrElse(backend, js.Dictionary.empty[SizeMetric]),
    metricValue = _.code,
    excludedKeys = Set("meta", "SUM")
  )
}
