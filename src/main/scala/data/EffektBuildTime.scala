package data

import plots.TimedLine
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.{AnnotationContext, BuildPoint, Json}

class EffektBuildTime(val dataPoints: js.Array[BuildPoint])(implicit annotationContext: AnnotationContext) extends TimedLine {
  override def chartTitle: String = "Time for Building Effekt"
  override def xLabel = "date"
  override def yLabel = "time in seconds"
  override def points = dataPoints

  def chartDataOpt = if (dataPoints.isEmpty) None else Some(new ChartData {
    labels = dataPoints.map(Json.date)
    datasets = js.Array(new ChartDataSets {
      label = "time (s)"
      data = dataPoints.map(_.buildTime.toDouble)
      fill = false
      backgroundColor = colorScheme.nextColor()
      borderColor = backgroundColor
    })
  })
}
