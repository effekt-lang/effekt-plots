package plots

import typings.chartJs.mod.*
import com.raquo.laminar.api.L.{*, given}

trait Generic {
  val chartConfig: ChartConfiguration

  def draw(): HtmlElement = {
    var optChart: Option[Chart] = None
    canvasTag(
      width := "100%",
      height := "500px",
      onMountUnmountCallback(
        mount = { nodeCtx => 
          val ctx = nodeCtx.thisNode.ref
          val chart = Chart.apply.newInstance2(ctx, chartConfig)
          optChart = Some(chart)
        },
        unmount = { thisNode => 
          optChart.map { _.destroy() }
          optChart = None
        }
      )
    )
  }
}
