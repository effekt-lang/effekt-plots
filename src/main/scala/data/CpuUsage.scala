package data

import plots.LineStacked
import scala.scalajs.js
import typings.chartJs.mod.*
import utils.Color

class CpuUsage(d: js.Array[js.Dynamic]) extends LineStacked {
  override lazy val chartTitle: String = "CPU Usage of Benchmarks"
  override lazy val xLabel = "benchmark date"
  override lazy val yLabel = "CPU usage in percent"

  override def tooltipBody(idx: Int) = js.Array(
    f"Commit: ${d(idx).meta.commit}",
    f"Commit date: ${new js.Date(d(idx).meta.commitDate.asInstanceOf[String].toDouble * 1000).toLocaleString()}"
  )

  lazy val chartData = {
    val keys = js.Object.keys(d(0).asInstanceOf[js.Object])
      .filter { k => k != "meta" && k != "total" }

    // new ChartData {
    //   labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
    //   datasets = js.Array(
    //     new ChartDataSets {
    //       label = "CPU usage (%)"
    //       data = d.map { s => s.cpuUsage.asInstanceOf[String].init.toInt }
    //       fill = false
    //       backgroundColor = colorScheme.nextColor()
    //       borderColor = backgroundColor
    //     }
    //   )
    // }
    new ChartData {
      labels = d.map { e => new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000) }
      datasets = keys.map { key =>
        new ChartDataSets {
          label = key
          data = d.map { _.selectDynamic(key).cpuUsage.asInstanceOf[String].init.toInt }
          backgroundColor = colorScheme.nextColor()
        }
      }
    }
  }
}
