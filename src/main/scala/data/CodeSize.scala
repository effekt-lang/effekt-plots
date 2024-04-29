package data

import plots.LineStacked
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color

class CodeSize(d: js.Array[js.Dynamic]) extends LineStacked {
  override lazy val chartTitle: String = "Code Size of Entire Repository"
  override lazy val xLabel = "benchmark date"
  override lazy val yLabel = "lines of code"

  lazy val chartData = {
    val keys = js.Object.keys(d(0).asInstanceOf[js.Object])
      .filter { k => k != "meta" && k != "SUM" }

    new ChartData {
      labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
      datasets = keys.map { key =>
        new ChartDataSets {
          label = key
          data = d.map { _.selectDynamic(key).code.asInstanceOf[Double] }
          backgroundColor = colorScheme.nextColor()
        }
      }
    }
  }
}