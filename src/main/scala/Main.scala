import data._
import utils._
import org.scalajs.dom
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.HTMLInputElement
import scalajs.js
import scala.collection.mutable.HashMap

val trackedPhaseDirectories = js.Array(
  "examples/casestudies/",
  // "/home/runner/work/effekt-plots/effekt-plots/effekt/libraries/",
)

val trackedBenchmarks = js.Array(
  "are_we_fast_yet",
  "duality_of_compilation",
  "effect_handlers_bench"
)

val trackedIndividualBuild = js.Array(
  "examples/casestudies/prettyprinter.effekt.md"
)

case class DateInterval(start: js.Date, end: js.Date)

case class Data(
  phases: js.Array[js.Dynamic],
  codeSize: js.Array[js.Dynamic],
  generatedCodeSize: js.Array[js.Dynamic],
  metrics: js.Array[js.Dynamic],
  buildTime: js.Array[js.Dynamic],
  backends: js.Array[js.Dynamic],
  annotations: js.Array[js.Dynamic],
)

def allDataInRange(interval: DateInterval) = Data(
  loadJsonByDate(interval, "phases"),
  loadJsonByDate(interval, "cloc"),
  loadJsonByDate(interval, "out_loc"),
  loadJsonByDate(interval, "metrics"),
  loadJsonByDate(interval, "build"),
  loadJsonByDate(interval, "backends"),
  loadJson("annotations"),
)

def loadJsonByDate(interval: DateInterval, name: String): js.Array[js.Dynamic] =
  val startYear = interval.start.getFullYear().toInt
  val endYear = interval.end.getFullYear().toInt
  val startMonth = interval.start.getMonth().toInt + 1
  val endMonth = interval.end.getMonth().toInt + 1

  var currentYear = startYear
  var currentMonth = startMonth
  var data = js.Array[js.Dynamic]()
  while (currentYear < endYear || (currentYear == endYear && currentMonth <= endMonth)) {
    data = data.concat(loadJson(s"$name/${currentYear}" + "%02d".format(currentMonth)))
    currentYear = if (currentMonth == 12) currentYear + 1 else currentYear
    currentMonth = if (currentMonth < 12) currentMonth + 1 else 1
  }
  data

var fileCache = HashMap[String, js.Array[js.Dynamic]]()
def loadJson(file: String): js.Array[js.Dynamic] = fileCache.getOrElse(file, {
  val xhr = XMLHttpRequest()
  xhr.open("get", s"data/$file.json", false) // TODO: this should be async
  xhr.send(null)
  val result = if (xhr.status == 200)
    js.JSON.parse(xhr.responseText).asInstanceOf[js.Array[js.Dynamic]]
    else js.Array()
  fileCache(file) = result
  result
})

def renderPhaseSection(prefix: String, phasesData: js.Array[js.Dynamic])(implicit C: AnnotationContext): HtmlElement = {
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

def renderBackendsSection(prefix: String, backendsData: js.Array[js.Dynamic])(implicit C: AnnotationContext): HtmlElement = {
  // filter the benchmarks in the data by prefix
  val preprocessor = SubstitutionPreprocessor(_.startsWith(prefix + "/"), _.replace(prefix + "/", ""))
  val filtered = preprocessor.filter(backendsData)

  sectionTag(
    h3(prefix, flexBasis.percent(100)),
    Backends(filtered, "llvm").draw(),
    Backends(filtered, "js").draw(),
    // BackendDiff(filtered, "js", "llvm").draw(),
  )
}

def renderMetricsSection(metricsData: js.Array[js.Dynamic])(implicit C: AnnotationContext): HtmlElement = {
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

def renderPlots(dateInterval: DateInterval): HtmlElement = {
  val allData = allDataInRange(dateInterval)

  given C: AnnotationContext = new AnnotationContext(allData.annotations)

  val preprocessor = new TimePreprocessor((date: js.Date) => {
    date.getTime > dateInterval.start.getTime && date.getTime < dateInterval.end.getTime
  })

  val phasesData = preprocessor.filter(allData.phases)
  val codeSizeData = preprocessor.filter(allData.codeSize)
  val generatedCodeSizeData = preprocessor.filter(allData.generatedCodeSize)
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
      GeneratedCodeSize(generatedCodeSizeData, "llvm").draw(),
      GeneratedCodeSize(generatedCodeSizeData, "js").draw(),
      CodeSize(codeSizeData).draw(),
      EffektBuildTime(buildTimeData).draw(),
    ),
  )
}

def nWeeksBack(weeks: Int) = {
    val today = new js.Date()
    today.setHours(-24 * 7 * weeks)
    today.toISOString().split('T')(0)
}

val view = {
  val renderBus = new EventBus[HtmlElement]
  val startDate = Var(nWeeksBack(4)) // default: 1 month
  val endDate = Var(new js.Date().toISOString().split('T')(0)) // today

  div(
    div(
      className := "control",
      input(
        typ := "date",
        value <-- startDate,
        onInput.mapToValue --> startDate
      ),
      "to",
      input(
        typ := "date",
        value <-- endDate,
        onInput.mapToValue --> endDate
      ),
      button(
        "generate",
        onClick --> { _ =>
          val start = new js.Date(startDate.now())
          val end = new js.Date(endDate.now())

          // set to start and end of day
          start.setUTCHours(0, 0, 0, 0)
          end.setUTCHours(23, 59, 59, 999)

          renderBus.emit(renderPlots(DateInterval(start, end)))
        }
      ),
    ),
    div(
      className := "control",
      button("last year", onClick --> {_ => startDate.set(nWeeksBack(52)) }),
      button("last month", onClick --> {_ => startDate.set(nWeeksBack(4)) }),
      button("last week", onClick --> {_ => startDate.set(nWeeksBack(1)) })
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
