package plots

import typings.chartJs.mod.*
import com.raquo.laminar.api.L.{*, given}

import scala.scalajs.js

trait LineStacked extends Generic {
  lazy val chartData: ChartData

  lazy val chartTitle = "Stacked Line Chart"
  lazy val xLabel = "x axis"
  lazy val yLabel = "y axis"

  val chartConfig = new ChartConfiguration {
    `type` = ChartType.line
    data = chartData
    options = new ChartOptions {
      responsive = true
      title = new ChartTitleOptions {
        display = true
        text = chartTitle
      }
      scales = new ChartScales {
        xAxes = js.Array(
          new ChartXAxe {
            `type` = "time"
            distribution = typings.chartJs.chartJsStrings.series
            time = new TimeScale {
              unit = TimeUnit.hour
              displayFormats = new TimeDisplayFormat {
                hour = "hA, D.M."
              }
            }
            stacked = true
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
            stacked = true
            scaleLabel = new ScaleTitleOptions {
              display = true
              labelString = yLabel
            }
          }
        )
      }
    }
  }
}
