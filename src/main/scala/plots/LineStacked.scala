package plots

import typings.chartJs.mod.*
import com.raquo.laminar.api.L.{*, given}

import scala.scalajs.js

trait LineStacked extends Generic {
  def chartDataOpt: Option[ChartData]

  def chartTitle = "Stacked Line Chart"
  def xLabel = "x axis"
  def yLabel = "y axis"

  def normalize(data: ChartData) = {
    val dat = data.datasets
    val multipliers: js.Array[Double] = dat.get.map { el => 1.0 / el.data.get(0).get.asInstanceOf[Double] }
    data.datasets = dat.get.zip(multipliers).map { case (el, multiplier) =>
      el.data = el.data.get.map { d => multiplier * d.get.asInstanceOf[Double] }
      el
    }
    data
  }

  val chartConfigOpt = chartDataOpt.map { chartData => new ChartConfiguration {
    `type` = ChartType.line
    data = if (normalizeData) normalize(chartData) else chartData
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
            stacked = true
            scaleLabel = new ScaleTitleOptions {
              display = true
              labelString = if (normalizeData) xLabel ++ " (normalized to 1)" else xLabel
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
              labelString = if (normalizeData) yLabel ++ " (normalized to 1)" else yLabel
            }
          }
        )
        onClick = clickHandler
      }
    }
  }}
}
