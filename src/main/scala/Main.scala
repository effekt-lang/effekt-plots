import data._
import utils._
import org.scalajs.dom
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom.XMLHttpRequest
import scalajs.js
import org.scalajs.dom.HTMLInputElement

val trackedPhaseDirectories = js.Array(
  "examples/casestudies/",
  "/home/runner/work/effekt-plots/effekt-plots/effekt/libraries/",
)

val trackedBenchmarks = js.Array(
  "are_we_fast_yet",
  "duality_of_compilation",
  "effect_handlers_bench"
)

val trackedIndividualBuild = js.Array(
  "examples/casestudies/prettyprinter.effekt.md"
)

case class Data(
  phases: js.Array[js.Dynamic],
  codeSize: js.Array[js.Dynamic],
  metrics: js.Array[js.Dynamic],
  buildTime: js.Array[js.Dynamic],
  backends: js.Array[js.Dynamic],
)

val allData = Data(
  loadFile("phases"),
  loadFile("cloc"),
  loadFile("metrics"),
  loadFile("build"),
  loadFile("backends"),
)

// TODO: this should be async
def loadFile(name: String): js.Array[js.Dynamic] = 
  val xhr = XMLHttpRequest()
  xhr.open("get", s"data/$name.json", false)
  xhr.send(null)
  js.JSON.parse(xhr.responseText).asInstanceOf[js.Array[js.Dynamic]]

def renderPhaseSection(prefix: String, phasesData: js.Array[js.Dynamic]): HtmlElement = {
  // filter the files in the data by prefix
  val preprocessor = SubstitutionPreprocessor(
    key => key.startsWith(prefix) || key.startsWith("./" + prefix),
    key => key.replace("./" + prefix, "").replace(prefix, "")
  )
  val filtered = preprocessor.filter(phasesData)

  sectionTag(
    h3(prefix, flexBasis.percent(100)),
    PhaseTimes(filtered).draw(),

    // the standard library doesn't have elements here, so we don't draw total if empty
    if (js.Object.keys(filtered(0).total.asInstanceOf[js.Object]).length > 0)
      PhaseTimesAccumulated(filtered).draw()
    else
      div(),
  )
}

def renderBackendsSection(prefix: String, backendsData: js.Array[js.Dynamic]): HtmlElement = {
  // filter the benchmarks in the data by prefix
  val preprocessor = SubstitutionPreprocessor(_.startsWith(prefix + "/"), _.replace(prefix + "/", ""))
  val filtered = preprocessor.filter(backendsData)

  sectionTag(
    h3(prefix, flexBasis.percent(100)),
    Backends(filtered, "llvm").draw(),
    Backends(filtered, "js").draw(),
    BackendDiff(filtered, "js", "llvm").draw(),
  )
}

def renderMetricsSection(metricsData: js.Array[js.Dynamic]): HtmlElement = {
  val files = trackedIndividualBuild

  val filtered = metricsData.map { dyn =>
    js.Object.fromEntries(
      js.Object.entries(dyn.asInstanceOf[js.Object]).filter { case js.Tuple2(key, value) =>
        files.contains(key) || files.contains("./" + key) || key == "meta"
      }
    ).asInstanceOf[js.Dynamic]
  }

  sectionTag(
    MemoryUsage(filtered).draw(),
    CompileTime(filtered).draw(),
    CpuUsage(filtered).draw(),
  )
}

def renderPlots(timeFilter: js.Date => Boolean): HtmlElement = {
  val preprocessor = new TimePreprocessor(timeFilter)

  val phasesData = preprocessor.filter(allData.phases)
  val codeSizeData = preprocessor.filter(allData.codeSize)
  val buildTimeData = preprocessor.filter(allData.buildTime)
  val metricsData = preprocessor.filter(allData.metrics)
  val backendsData = preprocessor.filter(allData.backends)

  // too much filtering, nothing left
  if (phasesData.isEmpty) return sectionTag()

  sectionTag(
    h2("Phase times", flexBasis.percent(100)),
    p("The time per phase is extracted using the Effekt `--time json` flag.", flexBasis.percent(100)),
    trackedPhaseDirectories.map { (dir: String) => renderPhaseSection(dir, phasesData) },
    h2("Backend benchmarks", flexBasis.percent(100)),
    trackedBenchmarks.map { (benchmarks: String) => renderBackendsSection(benchmarks, backendsData) },
    h2("Build metrics", flexBasis.percent(100)),
    p("The metrics are gathered by measuring `effekt -b <file>` using `gnutime`. Therefore, these metrics include the overhead of JVM.", flexBasis.percent(100)),
    renderMetricsSection(metricsData),
    h2("General metrics", flexBasis.percent(100)),
    sectionTag(
      CodeSize(codeSizeData).draw(),
      EffektBuildTime(buildTimeData).draw(),
    ),
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
          today.setHours(-24 * 7 * 4) // last month
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
