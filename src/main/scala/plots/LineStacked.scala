package plots

import typings.chartJs.mod.*
import scala.scalajs.js

trait LineStacked extends Generic {
  def chartDataOpt: Option[ChartData]

  def chartTitle = "Stacked Line Chart"
  def xLabel = "x axis"
  def yLabel = "y axis"

  def normalize(chartData: ChartData): ChartData = {
    chartData.datasets.get.foreach { dataset =>
      val values = dataset.data.get
      val firstNonZeroValue = values.map(_.get.asInstanceOf[Double]).find(_ != 0)
      val multiplier = firstNonZeroValue.map(1.0 / _).getOrElse(1.0)
      dataset.data = values.map(value => multiplier * value.get.asInstanceOf[Double])
    }
    chartData
  }

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
  }

  val chartConfigOpt = chartDataOpt.map { chartData =>
    new ChartConfiguration {
      `type` = ChartType.line
      data = if (normalizeData) normalize(chartData) else chartData
      options = chartOptions(chartScales, Some(lineElements))
    }
  }
}
