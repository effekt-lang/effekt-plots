package data

import plots.LineStacked
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.randomColor

class PhaseTimes(d: js.Array[js.Dynamic]) extends LineStacked {
  override lazy val chartTitle: String = "Phase Times of Benchmarks"
  override lazy val xLabel = "commit hash"
  override lazy val yLabel = "time in seconds"

  lazy val chartData = {
    val keys = js.Object.keys(d(0).asInstanceOf[js.Object])
      .filter { k => k != "meta" && k != "total" }

    new ChartData {
      labels = d.map { _.meta.commit.asInstanceOf[String] }
      datasets = keys.map { key =>
        new ChartDataSets {
          label = key
          data = d.map { p =>
            js.Object.entries(p.selectDynamic(key).asInstanceOf[js.Object]).foldLeft(0.0) {
                case (acc, js.Tuple2(_, s)) => acc + s.asInstanceOf[Double]
            }
          }
          backgroundColor = randomColor()
        }
      }
    }
  }
}