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

  def annotationPlugin(chart: Chart, options: js.UndefOr[Any]): Unit =
    val ctx = chart.ctx
    val testDate = new js.Date("Tue Jul 23 2024 14:52:00 GMT+0200")
    val index = chart.data.labels.get.indexWhere { l =>
      val date = l.asInstanceOf[js.Date]

      // time frame of 5 minutes since tests run sequentially
      (date.getTime - testDate.getTime).abs < 1000 * 60 * 5
    }

    if (index == -1)
      return

    val meta = chart.getDatasetMeta(0)
    val x = meta.data(index)._model.x
    // for some reason ScalablyTyped doesn't know this API
    val scale = chart.asInstanceOf[js.Dynamic].scales.`y-axis-0`
    val y1 = scale.bottom.asInstanceOf[Double]
    val y2 = scale.top.asInstanceOf[Double]
    drawLine(ctx, x, y1, y2)

    // add line click handler
    val canvas = chart.canvas
    val rect = canvas.getBoundingClientRect()
    chart.canvas.addEventListener("click", (event: js.Dynamic) => {
      val clickX = event.clientX.asInstanceOf[Double] - rect.left
      if (clickX > x - 5 && clickX < x + 5)
        dom.console.log("clicked line!")
      else dom.console.log(x - 5, clickX, x + 5)
      event.stopImmediatePropagation()
      event.stopPropagation()
    }, true)

  def draw(): HtmlElement = {
    var optChart: Option[Chart] = None
    val legend = div(className := "legend")
    div(
      legend,
      canvasTag(
        onMountUnmountCallback(
          mount = { nodeCtx => 
            val ctx = nodeCtx.thisNode.ref
            val plugin = PluginServiceRegistrationOptions().setAfterRender(
              annotationPlugin
            )
            val extendedConfig = chartConfig.set("plugins", js.Array(plugin))
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
