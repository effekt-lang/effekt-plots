package data

import plots.TimedLineStacked
import scala.scalajs.js
import utils.{AnnotationContext, BackendMetric, Fields, Json, TimedPoint}

class BackendsTime(val dataPoints: js.Array[TimedPoint], backend: String)(implicit annotationContext: AnnotationContext) extends TimedLineStacked {
  override def chartTitle: String = s"Execution Time for $backend Backend"
  override def xLabel = "date"
  override def yLabel = "time in seconds"
  override def points = dataPoints

  def chartDataOpt = seriesData(
    points = dataPoints,
    seriesAt = point => Json.fields[Fields[BackendMetric]](point).getOrElse(backend, js.Dictionary.empty[BackendMetric]),
    metricValue = _.time / 1e9,
    excludedKeys = Set("meta", "SUM")
  )
}
