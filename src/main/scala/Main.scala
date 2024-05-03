import data._
import utils._
import org.scalajs.dom
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom.XMLHttpRequest
import scalajs.js
import org.scalajs.dom.HTMLInputElement

// TODO: this should be async
def loadFile(name: String): js.Array[js.Dynamic] = 
  val xhr = XMLHttpRequest()
  xhr.open("get", s"data/$name.json", false)
  xhr.send(null)
  js.JSON.parse(xhr.responseText).asInstanceOf[js.Array[js.Dynamic]]

case class Data(phases: js.Array[js.Dynamic], codeSize: js.Array[js.Dynamic], metrics: js.Array[js.Dynamic], buildTime: js.Array[js.Dynamic])

val allData = Data(
  loadFile("phases"),
  loadFile("cloc"),
  loadFile("metrics"),
  loadFile("build")
)

def renderPlots(timeFilter: TimeFilter): HtmlElement = {
  val preprocessor = new Preprocessor(timeFilter)

  val phasesData = preprocessor.filter(allData.phases)
  val codeSizeData = preprocessor.filter(allData.codeSize)
  val metricsData = preprocessor.filter(allData.metrics)
  val buildTimeData = preprocessor.filter(allData.buildTime)

  // too much filtering, nothing left
  if (phasesData.isEmpty) return sectionTag()

  sectionTag(
    PhaseTimes(phasesData).draw(),
    ByBenchmark(phasesData).draw(),
    CodeSize(codeSizeData).draw(),
    BuildTime(buildTimeData).draw(),
    MemoryUsage(metricsData).draw(),
    TimeMeasure(metricsData).draw(),
    CpuUsage(metricsData).draw()
  )
}

val view = {
  val renderBus = new EventBus[HtmlElement]

  div(
    div(
      idAttr := "control",
      input(
        idAttr := "startDate",
        `type` := "date",
        value := {
          val today = new js.Date()
          today.setHours(-24 * 7) // last week
          today.toISOString().split('T')(0)
        }
      ),
      "to",
      input(
        idAttr := "endDate",
        `type` := "date",
        value := new js.Date().toISOString().split('T')(0) // today
      ),
      button(
        "generate",
        onClick --> { _ =>
          val startDate = new js.Date(dom.document.getElementById("startDate").asInstanceOf[HTMLInputElement].value)
          val endDate = new js.Date(dom.document.getElementById("endDate").asInstanceOf[HTMLInputElement].value)

          // set to start and end of day
          startDate.setUTCHours(0, 0, 0, 0)
          endDate.setUTCHours(23, 59, 59, 999)

          val filterFunction = (date: js.Date) => {
            date.getTime > startDate.getTime && date.getTime < endDate.getTime
          }
          renderBus.emit(renderPlots(filterFunction))
        }
      )
    ),
    div(
      idAttr := "main",
      child <-- renderBus
    )
  )
}

@main
def main(): Unit =
  renderOnDomContentLoaded(dom.document.getElementById("main"), view)
