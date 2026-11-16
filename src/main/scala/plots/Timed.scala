package plots

import scala.scalajs.js
import typings.chartJs.mod.*
import utils.{Fields, Json, TimedPoint}

trait Timed extends Generic {
  def points: js.Array[? <: TimedPoint]
  override def commitAt(index: Int) = Some(points(index).meta)

  protected def seriesData[P <: TimedPoint, A](
    points: js.Array[P],
    seriesAt: P => Fields[A],
    metricValue: A => Double,
    excludedKeys: Set[String],
    fillArea: Boolean = true
  ): Option[ChartData] = {
    val seriesKeys = Json.seriesKeys(points, seriesAt).filterNot(excludedKeys)
    if (seriesKeys.isEmpty) None else Some(new ChartData {
      labels = points.map(Json.date)
      datasets = seriesKeys.map { key =>
        val color = colorScheme.nextColor()
        new ChartDataSets {
          label = key
          data = points.map(point => seriesAt(point).get(key).map(metricValue).getOrElse(0.0))
          backgroundColor = color
          if (!fillArea) {
            borderColor = color
            fill = false
          }
        }
      }
    })
  }

  override def tooltipBody(index: Int): js.Array[String] = {
    val meta = points(index).meta
    js.Array(
      s"Commit: ${meta.commit}",
      s"Commit date: ${new js.Date(meta.commitDate.toDouble * 1000).toLocaleString()}"
    )
  }
}

trait TimedLine extends Line with Timed
trait TimedLineStacked extends LineStacked with Timed
