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
            // `type` = "category"
            stacked = true
            // labels = js.Array("1", "2", "3")
            // title = new ChartTitleOptions {
            //   text = xLabel
            // }
            // display = true
          }
        )
        yAxes = js.Array(
          new ChartYAxe {
            `type` = "linear"
            ticks = new TickOptions {
              beginAtZero = true
            }
            // labels = js.Array("1", "2", "3")
            stacked = true
            // title = new ChartTitleOptions {
            //   text = yLabel
            // }
            // display = true
          }
        )
      }
    }
  }
}
