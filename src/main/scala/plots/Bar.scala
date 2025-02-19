package plots

import typings.chartJs.mod.*
import com.raquo.laminar.api.L.{*, given}

import scala.scalajs.js
import org.scalajs.dom

trait Bar extends Generic {
  def chartDataOpt: Option[ChartData]

  def chartTitle = "Bar Chart"
  def xLabel = "x axis"
  def yLabel = "y axis"

  // Annotations are not supported in Bar charts since they are categorical, not time-based
  override def annotationPlugin(chart: Chart, easing: Easing, options: js.UndefOr[Any]): Unit = ()

  val chartConfigOpt = chartDataOpt.map { chartData => new ChartConfiguration {
    `type` = ChartType.bar
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
        onClick = clickHandler
      }
    }
  }}
}
