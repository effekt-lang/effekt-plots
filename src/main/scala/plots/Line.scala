package plots

import typings.chartJs.mod.*
import com.raquo.laminar.api.L.{*, given}

import scala.scalajs.js

trait Line extends Generic {
  // x values of the plot
  val x: js.Array[Double]

  // f(x) values of the plot
  val y: js.Array[Double]

  val chartConfig = new ChartConfiguration {
    `type` = ChartType.bar
    data = new ChartData {
      labels = js.Array(1,2,3)
      datasets = js.Array(new ChartDataSets {
        label = "Value"
        borderWidth = 1
        data = js.Array(1,2,3)
      })
    }
    options = new ChartOptions {
      scales = new ChartScales {
        yAxes = js.Array(new CommonAxe {
          ticks = new TickOptions {
            beginAtZero = true
          }
        })
      }
    }
  }
}
