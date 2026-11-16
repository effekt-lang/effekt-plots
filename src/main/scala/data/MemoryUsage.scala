package data

import plots.TimedLineStacked
import scala.scalajs.js
import utils.{AnnotationContext, Json, Metric, TimedPoint}

class MemoryUsage(val dataPoints: js.Array[TimedPoint])(implicit annotationContext: AnnotationContext) extends TimedLineStacked {
  override def chartTitle: String = "Maximum Memory while Building"
  override def xLabel = "date"
  override def yLabel = "memory usage in kilobyte"
  override def points = dataPoints

  def chartDataOpt = seriesData(
    points = dataPoints,
    seriesAt = point => Json.fields[Metric](point),
    metricValue = _.maxMem.toDouble,
    excludedKeys = Set("meta", "total"),
    fillArea = false
  )
}
