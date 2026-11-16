package plots

import typings.chartJs.mod.*
import scala.scalajs.js

trait Bar extends Generic {
  def chartDataOpt: Option[ChartData]

  def chartTitle = "Bar Chart"
  def xLabel = "x axis"
  def yLabel = "y axis"

  override def annotationPlugin(chart: Chart, easing: Easing, options: js.UndefOr[Any]): Unit = ()

  private def chartScales = new ChartScales {
    xAxes = js.Array(
      new ChartXAxe {
        `type` = "category"
        distribution = typings.chartJs.chartJsStrings.series
        scaleLabel = new ScaleTitleOptions {
          display = true
          labelString = xLabel
        }
      }
    )
    yAxes = js.Array(
      new ChartYAxe {
        `type` = "linear"
        ticks = new TickOptions {
          beginAtZero = true
        }
        scaleLabel = new ScaleTitleOptions {
          display = true
          labelString = yLabel
        }
      }
    )
  }

  val chartConfigOpt = chartDataOpt.map { chartData =>
    val barOptions = chartOptions(chartScales)
    barOptions.tooltips.get.mode = typings.chartJs.chartJsStrings.index
    barOptions.tooltips.get.intersect = false
    new ChartConfiguration {
      `type` = ChartType.bar
      data = chartData
      options = barOptions
    }
  }
}
