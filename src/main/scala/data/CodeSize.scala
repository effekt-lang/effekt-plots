package data

import plots.LineStacked
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.randomColor

class codeSize(d: js.Array[js.Dynamic]) extends LineStacked {
  override lazy val chartTitle: String = "Code Size"
  override lazy val xLabel = "commit hash"
  override lazy val yLabel = "lines of code"

  lazy val chartData = {
    val keys = js.Object.keys(d(0).asInstanceOf[js.Object])
      .filter { k => k != "meta" && k != "SUM" }

    new ChartData {
      labels = d.map { _.selectDynamic("meta").selectDynamic("commit").asInstanceOf[String] }
      datasets = keys.map { key =>
        new ChartDataSets {
          label = key
          data = d.map { _.selectDynamic(key).selectDynamic("code").asInstanceOf[Double] }
          backgroundColor = randomColor()
        }
      }
    }
  }
}