package data

import plots.TimedLineStacked
import scala.scalajs.js
import utils.{AnnotationContext, Fields, Json, PhasePoint}

class PhaseTimes(val dataPoints: js.Array[PhasePoint])(implicit annotationContext: AnnotationContext) extends TimedLineStacked {
  override def chartTitle: String = "Phase Times of Programs"
  override def xLabel = "date"
  override def yLabel = "time in seconds"
  override def points = dataPoints

  def chartDataOpt = seriesData(
    points = dataPoints,
    seriesAt = point => Json.fields[Fields[Double]](point),
    metricValue = _.values.foldLeft(0.0)(_ + _) / 1000,
    excludedKeys = Set("meta", "total", "type")
  )
}
