package data

import plots.TimedLineStacked
import scala.scalajs.js
import utils.{AnnotationContext, Json, SizeMetric, TimedPoint}

class CodeSize(val dataPoints: js.Array[TimedPoint])(implicit annotationContext: AnnotationContext) extends TimedLineStacked {
  override def chartTitle: String = "Code Size of Entire Repository"
  override def xLabel = "date"
  override def yLabel = "lines of code"
  override def points = dataPoints

  def chartDataOpt = seriesData(
    points = dataPoints,
    seriesAt = point => Json.fields[SizeMetric](point),
    metricValue = _.code,
    excludedKeys = Set("meta", "SUM")
  )
}
