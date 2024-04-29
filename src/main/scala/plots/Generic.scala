package plots

import typings.chartJs.mod.*
import com.raquo.laminar.api.L.{*, given}
import utils.Color
import org.scalajs.dom

trait Generic {
  val chartConfig: ChartConfiguration

  val colorScheme = Color()

  def draw(): HtmlElement = {
    var optChart: Option[Chart] = None
    canvasTag(
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
