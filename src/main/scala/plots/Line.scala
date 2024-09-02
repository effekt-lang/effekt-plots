package plots

import typings.chartJs.mod.*
import com.raquo.laminar.api.L.{*, given}

import scala.scalajs.js
import org.scalajs.dom

trait Line extends Generic {
  def chartDataOpt: Option[ChartData]

  def chartTitle = "Line Chart"
  def xLabel = "x axis"
  def yLabel = "y axis"

  def tooltipBody(idx: Int) = js.Array("")

  val chartConfigOpt = chartDataOpt.map { chartData => new ChartConfiguration {
    `type` = ChartType.line
    data = chartData
    options = new ChartOptions {
      responsive = true
      maintainAspectRatio = false
      title = new ChartTitleOptions {
        display = true
        text = chartTitle
      }
      tooltips = new ChartTooltipOptions {
        callbacks = new ChartTooltipCallback {
          afterBody = (items: js.Array[ChartTooltipItem], data: ChartData) =>
            items(0).index.map { idx => tooltipBody(idx.toInt) }
              .getOrElse(js.Array(""))
        }
      }
      legend = new ChartLegendOptions {
        display = false
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
    }
  }}
}
