package plots

import scalajs.js
import typings.chartJs.mod.*
import com.raquo.laminar.api.L.{*, given}
import utils.Color
import org.scalajs.dom

trait Generic {
  val chartConfig: ChartConfiguration

  val colorScheme = Color()

  def drawLine(ctx: dom.CanvasRenderingContext2D, x: Double, y1: Double, y2: Double) = {
    ctx.save()
    ctx.beginPath()
    ctx.moveTo(x, y1)
    ctx.lineTo(x, y2)
    ctx.lineWidth = 1
    ctx.strokeStyle = "red"
    ctx.stroke()
    ctx.restore()
  }

  val annotationPlugin = PluginServiceRegistrationOptions().setAfterDatasetDraw(
    (chart, easing, options) => {
      val ctx = chart.ctx
      val testDate = new js.Date("Tue Jul 23 2024 14:52:00 GMT+0200")
      val index = chart.data.labels.get.indexWhere { l =>
        val date = l.asInstanceOf[js.Date]

        // time frame of 5 minutes since tests run sequentially
        (date.getTime - testDate.getTime).abs < 1000 * 60 * 5
      }

      if (index != -1) {
        val meta = chart.getDatasetMeta(0)
        val x = meta.data(index)._model.x
        // for some reason ScalablyTyped doesn't know this API
        val scale = chart.asInstanceOf[js.Dynamic].scales.`y-axis-0`
        drawLine(ctx, x, scale.bottom.asInstanceOf[Double], scale.top.asInstanceOf[Double])
      }
    }
  )

  def draw(): HtmlElement = {
    var optChart: Option[Chart] = None
    val legend = div(className := "legend")
    div(
      legend,
      canvasTag(
        onMountUnmountCallback(
          mount = { nodeCtx => 
            val ctx = nodeCtx.thisNode.ref
            val extendedConfig = chartConfig.set("plugins", js.Array(annotationPlugin))
            val chart = Chart.apply.newInstance2(ctx, extendedConfig)
            legend.ref.innerHTML = chart.generateLegend().toString()
            legend.ref.children(0).children.zipWithIndex.foreach { (child, index) =>
              child.addEventListener("click", _ => {
                val meta = chart.getDatasetMeta(index)
                meta.hidden = !meta.hidden.getOrElse(true)
                chart.update()
                child.classList.toggle("legend-unselected")
              })
            }
            optChart = Some(chart)
          },
          unmount = { thisNode => 
            optChart.map { _.destroy() }
            optChart = None
          }
        )
      )
    )
  }
}
