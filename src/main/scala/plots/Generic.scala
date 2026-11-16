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

  def tooltipBody(index: Int) = js.Array("")
  def commitAt(index: Int): Option[Meta] = None

  // TODO: This should probably be somewhere else
  def normalizeData = C.normalized

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

  def showAnnotation(reason: String) = fire(title = "Reason", html = reason)

  def showCommits(meta: Meta): Unit = {
    val commitIndex = annotationContext.commits.indexWhere(_.hash.startsWith(meta.commit))
    val nearbyCommits = if (commitIndex < 0) js.Array[utils.CommitJson]()
    else annotationContext.commits.slice(
      math.max(0, commitIndex - 3),
      math.min(annotationContext.commits.length, commitIndex + 4)
    )
    val commitItems = nearbyCommits.map { commit =>
      val escapedMessage = commit.message
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
      s"<li><a href='https://github.com/effekt-lang/effekt/commit/${commit.hash}' target='_blank' rel='noopener noreferrer'>${commit.hash.take(10)}</a> $escapedMessage</li>"
    }
    val popupHtml = if (commitItems.isEmpty)
      s"<a href='https://github.com/effekt-lang/effekt/commit/${meta.commit}' target='_blank' rel='noopener noreferrer'>${meta.commit}</a>"
    else s"<ul class='commit-list'>${commitItems.join("")}</ul>"
    fire(title = "Nearby commits", html = popupHtml)
  }

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

        val handlerKey = (chartId, annotation.id)
        annotationContext.annotationClickHandlers.get(handlerKey).foreach { previousHandler =>
          chart.canvas.removeEventListener("click", previousHandler, false)
        }
        val annotationClickHandler: js.Function1[dom.MouseEvent, Unit] = (event: dom.MouseEvent) => {
          val canvasLeft = chart.canvas.getBoundingClientRect().left
          if ((event.clientX - canvasLeft - xPosition).abs < 10)
            showAnnotation(annotation.reason)
        }
        chart.canvas.addEventListener("click", annotationClickHandler, false)
        annotationContext.annotationClickHandlers(handlerKey) = annotationClickHandler
      }
    }
  
  def clickHandler(event: js.UndefOr[dom.MouseEvent], activeElements: js.UndefOr[js.Array[js.Object]]) = {
    if (activeElements.isDefined && !activeElements.get.isEmpty) {
      val chart = activeElements.get(0).asInstanceOf[ChartElement].`_chart`
      val clickedElements = chart.getElementAtEvent(event)
      if (clickedElements.length == 1) {
        val pointIndex = clickedElements(0).asInstanceOf[ChartElement].`_index`
        commitAt(pointIndex) match {
          case Some(meta) => showCommits(meta)
          case None => fire(title = "Details", html = tooltipBody(pointIndex).join("<br>"))
        }
      }

      // do not activate the annotation popup
      event.get.stopImmediatePropagation()
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
