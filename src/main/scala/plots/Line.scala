package plots

import typings.chartJs.mod.*
import scala.scalajs.js

trait Line extends Generic {
  def chartDataOpt: Option[ChartData]

  def chartTitle = "Line Chart"
  def xLabel = "x axis"
  def yLabel = "y axis"

  private def chartScales = new ChartScales {
    xAxes = js.Array(
      new ChartXAxe {
        `type` = "time"
        distribution = typings.chartJs.chartJsStrings.series
        time = new TimeScale {
          displayFormats = new TimeDisplayFormat {
            hour = "hA, D.M."
          }
        }
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
    new ChartConfiguration {
      `type` = ChartType.line
      data = chartData
      options = chartOptions(chartScales, Some(lineElements))
    }
  }
}
