package plots

import scalajs.js
import typings.chartJs.mod.*
import typings.sweetalert2.mod.default.*
import com.raquo.laminar.api.L.{*, given}
import utils.{AnnotationContext, Color, Meta}
import org.scalajs.dom

trait Generic(implicit annotationContext: AnnotationContext) {
  val chartConfigOpt: Option[ChartConfiguration]
  def chartTitle: String

  val colorScheme = Color()

  def tooltipBody(index: Int) = js.Array("")
  def commitAt(index: Int): Option[Meta] = None

  def normalizeData = annotationContext.normalized

  def drawLine(canvasContext: dom.CanvasRenderingContext2D, xPosition: Double, yBottom: Double, yTop: Double) = {
    canvasContext.save()
    canvasContext.beginPath()
    canvasContext.moveTo(xPosition, yBottom)
    canvasContext.lineTo(xPosition, yTop)
    canvasContext.lineWidth = 1
    canvasContext.strokeStyle = "red"
    canvasContext.stroke()
    canvasContext.restore()
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
    val canvasContext = chart.ctx
    val chartId = chart.asInstanceOf[ChartInternals].id

    annotationContext.annotations.foreach { annotation =>
      val pointIndex = chart.data.labels.get.indexWhere { label =>
        val date = label.asInstanceOf[js.Date]
        (date.getTime - annotation.date.getTime).abs < 1000 * 60 * 20
      }

      if (pointIndex >= 0) {
        val dataset = chart.getDatasetMeta(0)
        val xPosition = dataset.data(pointIndex)._model.x
        val yScale = chart.asInstanceOf[ChartInternals].scales("y-axis-0")
        drawLine(canvasContext, xPosition, yScale.bottom, yScale.top)

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

  def chartOptions(chartScales: ChartScales, chartElements: Option[ChartElementsOptions] = None): ChartOptions = new ChartOptions {
    responsive = true
    maintainAspectRatio = false
    animation = new ChartAnimationOptions { duration = 0 }
    chartElements.foreach(configuredElements => elements = configuredElements)
    title = new ChartTitleOptions {
      display = true
      text = chartTitle
    }
    tooltips = new ChartTooltipOptions {
      callbacks = new ChartTooltipCallback {
        afterBody = (items: js.Array[ChartTooltipItem], _: ChartData) =>
          if (items.isEmpty) js.Array("")
          else items(0).index.map(index => tooltipBody(index.toInt)).getOrElse(js.Array(""))
      }
    }
    legend = new ChartLegendOptions { display = false }
    onClick = clickHandler
    scales = chartScales
  }

  def lineElements: ChartElementsOptions = new ChartElementsOptions {
    point = new ChartPointOptions { radius = 0; hitRadius = 5 }
    line = new ChartLineOptions { tension = 0 }
  }

  def drawPlot(legend: HtmlElement, chartConfig: ChartConfiguration): HtmlElement =
    var mountedChart: Option[Chart] = None
    canvasTag(
      onMountUnmountCallback(
        mount = { nodeCtx =>
          val canvas = nodeCtx.thisNode.ref
          val pluginRegistration = PluginServiceRegistrationOptions().setBeforeDatasetDraw(annotationPlugin)
          val configurationWithAnnotations = chartConfig.set("plugins", js.Array(pluginRegistration))
          val chart = Chart.apply.newInstance2(canvas, configurationWithAnnotations)
          legend.ref.innerHTML = chart.generateLegend().toString()
          legend.ref.children(0).children.zipWithIndex.foreach { (legendItem, datasetIndex) =>
            legendItem.addEventListener("click", _ => {
              val dataset = chart.getDatasetMeta(datasetIndex)
              dataset.hidden = !dataset.hidden.getOrElse(true)
              chart.update()
              legendItem.classList.toggle("legend-unselected")
            })
          }
          if (chart.data.datasets.get.exists(_.label.isDefined)) {
            val hideAll = dom.document.createElement("li")
            hideAll.innerHTML = "<span class='legend-clear-icon'>✕</span>hide all"
            hideAll.addEventListener("click", _ => {
              val datasetIndices = chart.data.datasets.get.indices
              val hide = !datasetIndices.forall(index => chart.getDatasetMeta(index).hidden.getOrElse(false))
              datasetIndices.foreach { index =>
                chart.getDatasetMeta(index).hidden = hide
                legend.ref.children(0).children(index).classList.toggle("legend-unselected", hide)
              }
              chart.update()
            })
            legend.ref.children(0).appendChild(hideAll)
          }
          mountedChart = Some(chart)
        },
        unmount = { _ =>
          mountedChart.foreach(_.destroy())
          mountedChart = None
        }
      )
    )

  def draw(): HtmlElement = {
    chartConfigOpt.map { chartConfig =>
      val legend = div(className := "legend")
      div(legend, drawPlot(legend, chartConfig))
    }.getOrElse(div())
  }
}

@js.native
trait ChartInternals extends js.Object {
  val id: Int = js.native
  val scales: js.Dictionary[ChartScale] = js.native
}

@js.native
trait ChartScale extends js.Object {
  val top: Double = js.native
  val bottom: Double = js.native
}

@js.native
trait ChartElement extends js.Object {
  val `_chart`: Chart = js.native
  val `_index`: Int = js.native
}
