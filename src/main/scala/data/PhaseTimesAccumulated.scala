package data

import plots.TimedLineStacked
import scala.scalajs.js
import utils.{AnnotationContext, PhasePoint}

class PhaseTimesAccumulated(val dataPoints: js.Array[PhasePoint])(implicit annotationContext: AnnotationContext) extends TimedLineStacked {
  override def chartTitle: String = "Accumulated Phase Times per Program"
  override def xLabel = "date"
  override def yLabel = "time in seconds"
  override def points = dataPoints

  def chartDataOpt = seriesData(
    points = dataPoints,
    seriesAt = _.total,
    metricValue = _ / 1000,
    excludedKeys = Set.empty[String]
  )
}
