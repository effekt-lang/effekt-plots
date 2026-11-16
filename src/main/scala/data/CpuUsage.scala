package data

import plots.TimedLineStacked
import scala.scalajs.js
import utils.{AnnotationContext, Json, Metric, TimedPoint}

class CpuUsage(val dataPoints: js.Array[TimedPoint])(implicit annotationContext: AnnotationContext) extends TimedLineStacked {
  override def chartTitle: String = "CPU Usage of Programs"
  override def xLabel = "date"
  override def yLabel = "CPU usage in percent"
  override def points = dataPoints

  def chartDataOpt = seriesData(
    points = dataPoints,
    seriesAt = point => Json.fields[Metric](point),
    metricValue = _.cpuUsage.dropRight(1).toDouble,
    excludedKeys = Set("meta", "total"),
    fillArea = false
  )
}
