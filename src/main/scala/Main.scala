import data._
import org.scalajs.dom
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom.XMLHttpRequest
import scalajs.js

// TODO: this should be async
def loadData(name: String): js.Array[js.Dynamic] = 
  val xhr = XMLHttpRequest()
  xhr.open("get", s"data/$name.json", false)
  xhr.send(null)
  js.JSON.parse(xhr.responseText).asInstanceOf[js.Array[js.Dynamic]]

def renderPlots(timeFilter: js.Date => Boolean) =
  val phasesData = loadData("phases")
  val codeSizeData = loadData("cloc")
  val metricsData = loadData("metrics")
  val buildTimeData = loadData("build")
  sectionTag(
    PhaseTimes(phasesData).draw(),
    BuildTime(buildTimeData).draw(),
    CodeSize(codeSizeData).draw(),
    MemoryUsage(metricsData).draw(),
    TimeMeasure(metricsData).draw(),
    CpuUsage(metricsData).draw()
  )

@main
def main(): Unit =
  val plots = renderPlots(_ => true)
  renderOnDomContentLoaded(dom.document.getElementById("main"), plots)
