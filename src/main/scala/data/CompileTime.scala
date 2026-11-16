package data

import plots.TimedLineStacked
import scala.scalajs.js
import utils.{AnnotationContext, Json, Metric, TimedPoint}

class CompileTime(val dataPoints: js.Array[TimedPoint])(implicit annotationContext: AnnotationContext) extends TimedLineStacked {
  override def chartTitle: String = "Compile Time of Programs"
  override def xLabel = "date"
  override def yLabel = "time in seconds"
  override def points = dataPoints

  def chartDataOpt = seriesData(
    points = dataPoints,
    seriesAt = point => Json.fields[Metric](point),
    metricValue = _.userTime.toDouble,
    excludedKeys = Set("meta", "total"),
    fillArea = false
  )
}
