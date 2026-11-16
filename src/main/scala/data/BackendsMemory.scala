package data

import plots.TimedLineStacked
import scala.scalajs.js
import utils.{AnnotationContext, BackendMetric, Fields, Json, TimedPoint}

class BackendsMemory(val dataPoints: js.Array[TimedPoint], backend: String)(implicit annotationContext: AnnotationContext) extends TimedLineStacked {
  override def chartTitle: String = s"Maximum Memory for $backend Backend"
  override def xLabel = "date"
  override def yLabel = "memory usage in kilobyte"
  override def points = dataPoints

  def chartDataOpt = seriesData(
    points = dataPoints,
    seriesAt = point => Json.fields[Fields[BackendMetric]](point).getOrElse(backend, js.Dictionary.empty[BackendMetric]),
    metricValue = _.maxMem.toOption.getOrElse(0.0),
    excludedKeys = Set("meta", "SUM")
  )
}
