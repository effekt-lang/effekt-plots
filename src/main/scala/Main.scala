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

case class Data(
  phases: js.Array[js.Dynamic],
  codeSize: js.Array[js.Dynamic],
  metrics: js.Array[js.Dynamic],
  buildTime: js.Array[js.Dynamic],
)

// TODO: figure this out automatically
val trackedDirectories = js.Array(
  "examples/benchmarks/",
  "examples/casestudies/",
  "/home/runner/work/effekt-plots/effekt-plots/effekt/libraries/",
)

val allData = Data(
  loadFile("phases"),
  loadFile("cloc"),
  loadFile("metrics"),
  loadFile("build"),
)

def renderBenchmarkSection(prefix: String, phasesData: js.Array[js.Dynamic]): HtmlElement = {
  val filtered = phasesData.map { dyn =>
    js.Object.fromEntries(
      js.Object.keys(dyn.asInstanceOf[js.Object]).map { (phase: String) =>
        js.Tuple2(
          phase,
          js.Object.fromEntries(
            js.Object.entries(dyn.selectDynamic(phase).asInstanceOf[js.Object]).filter { case js.Tuple2(key, value) =>
              key.startsWith(prefix) || key.startsWith("./" + prefix) || phase == "meta"
            }.map { case js.Tuple2(key, value) =>
              js.Tuple2(key.replace("./" + prefix, "").replace(prefix, ""), value)
            }
          )
        )
      }
    ).asInstanceOf[js.Dynamic]
  }

  sectionTag(
    h3(prefix, flexBasis.percent(100)),
    PhaseTimes(filtered).draw(),
    ByBenchmark(filtered).draw(),
  )
}

def renderMetricsSection(prefix: String, metricsData: js.Array[js.Dynamic]): HtmlElement = {
  val filtered = metricsData.map { dyn =>
    js.Object.fromEntries(
      js.Object.entries(dyn.asInstanceOf[js.Object]).filter { case js.Tuple2(key, value) =>
        key.startsWith(prefix) || key.startsWith("./" + prefix) || key == "meta"
      }.map { case js.Tuple2(key, value) =>
        js.Tuple2(key.replace("./" + prefix, "").replace(prefix, ""), value)
      }
    ).asInstanceOf[js.Dynamic]
  }

  sectionTag(
    h3(prefix, flexBasis.percent(100)),
    MemoryUsage(filtered).draw(),
    TimeMeasure(filtered).draw(),
    CpuUsage(filtered).draw(),
  )
}

def renderPlots(timeFilter: TimeFilter): HtmlElement = {
  val preprocessor = new Preprocessor(timeFilter)

  // we split benchmarks measured by `effekt --time json` into build-only (`-b`) and including execution (acc)

  val phasesData = preprocessor.filter(allData.phases)
  val codeSizeData = preprocessor.filter(allData.codeSize)
  val buildTimeData = preprocessor.filter(allData.buildTime)
  val metricsData = preprocessor.filter(allData.metrics)

  // too much filtering, nothing left
  if (phasesData.isEmpty) return sectionTag()

  sectionTag(
    h2("Benchmark phase times (including execution: `effekt <files>`)", flexBasis.percent(100)),
    trackedDirectories.map { (dir: String) => renderBenchmarkSection(dir, phasesData) },
    h2("Individual benchmark metrics (`effekt -b <file>`)", flexBasis.percent(100)),
    trackedDirectories.map { (dir: String) => renderMetricsSection(dir, metricsData) },
    // MemoryUsage(metricsData).draw(),
    // TimeMeasure(metricsData).draw(),
    // CpuUsage(metricsData).draw(),
    h2("General metrics", flexBasis.percent(100)),
    CodeSize(codeSizeData).draw(),
    BuildTime(buildTimeData).draw(),
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
