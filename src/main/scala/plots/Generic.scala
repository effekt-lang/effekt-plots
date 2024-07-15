package plots

import typings.chartJs.mod.*
import com.raquo.laminar.api.L.{*, given}
import utils.Color

trait Generic {
  val chartConfig: ChartConfiguration

  val colorScheme = Color()

  def draw(): HtmlElement = {
    var optChart: Option[Chart] = None
    val legend = div(className := "legend")
    div(
      legend,
      canvasTag(
        onMountUnmountCallback(
          mount = { nodeCtx => 
            val ctx = nodeCtx.thisNode.ref
            val chart = Chart.apply.newInstance2(ctx, chartConfig)
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
