package plots

import scalajs.js
import typings.chartJs.mod.*
import typings.sweetalert2.mod.default.*
import com.raquo.laminar.api.L.{*, given}
import utils.Color
import utils.AnnotationContext
import scala.util.boundary
import org.scalajs.dom

trait Generic(implicit C: AnnotationContext) {
  val chartConfigOpt: Option[ChartConfiguration]

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

  def showAnnotation(reason: String) =
    fire(
      title="Reason",
      html=reason
    )

  def annotationPlugin(chart: Chart, easing: Easing, options: js.UndefOr[Any]): Unit =
    val ctx = chart.ctx
    val chartId = chart.asInstanceOf[js.Dynamic].id.asInstanceOf[Int]

    C.annotations.foreach { annotation =>
      boundary {
        val index = chart.data.labels.get.indexWhere { l =>
          val date = l.asInstanceOf[js.Date]

          // time frame of 20 minutes since tests run sequentially
          // includes effekt build time, so this is chosen very broadly
          (date.getTime - annotation.date.getTime).abs < 1000 * 60 * 20
        }

        // specific date not found (e.g. not in filtered range)
        if (index == -1)
          boundary.break()

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
        val clickHandler: js.Function1[js.Dynamic, Unit] = (event: js.Dynamic) => {
          val clickX = event.clientX.asInstanceOf[Double] - rect.left
          if (clickX > x - 10 && clickX < x + 10)
            showAnnotation(annotation.reason)
        }
        chart.canvas.addEventListener("click", clickHandler, false)

        // here we need to remove the old listener to avoid multiple handler invocations
        //   because plugin afterDraw functions get called on every redraw
        //   also: we can't use stopPropation() because it would disable handlers of other annotations in the same plot
        if (C.handlers.contains((chartId, annotation.id)))
          chart.canvas.removeEventListener("click", C.handlers((chartId, annotation.id)), false)
        C.handlers((chartId, annotation.id)) = clickHandler
      }
    }
  
  def drawPlot(legend: HtmlElement, chartConfig: ChartConfiguration): HtmlElement =
    var optChart: Option[Chart] = None
    canvasTag(
      onMountUnmountCallback(
        mount = { nodeCtx => 
          val ctx = nodeCtx.thisNode.ref
          val plugin = PluginServiceRegistrationOptions().setBeforeDatasetDraw(
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

  def draw(): HtmlElement = {
    val legend = div(className := "legend")
    chartConfigOpt.map { chartConfig =>
      div(
        legend,
        drawPlot(legend, chartConfig)
      )
    }.getOrElse(div())
  }
}
