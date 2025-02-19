import data._
import utils._
import org.scalajs.dom
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.HTMLInputElement
import scala.concurrent.Future
import scala.collection.mutable.HashMap
import dom.ext.Ajax
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import scalajs.js

import org.scalajs.dom

val trackedPhaseDirectories = js.Array(
  "examples/casestudies/",
  // "/home/runner/work/effekt-plots/effekt-plots/effekt/libraries/",
)

val trackedBenchmarks = js.Array(
  "are_we_fast_yet",
  "duality_of_compilation",
  "effect_handlers_bench",
  "input_output"
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
  backendReference: js.Array[js.Dynamic],
  annotations: js.Array[js.Dynamic],
  reference: js.Array[js.Dynamic],
)

def allDataInRange(interval: DateInterval) =
  val load = loadJsonByDate(interval)
  for {
    phases <- load("phases")
    codeSize <- load("cloc")
    generatedCodeSize <- load("out_loc")
    metrics <- load("metrics")
    buildTime <- load("build")
    backends <- load("backends")
    backendReference <- load("reference")
    annotations <- loadJson("annotations.json")
    reference <- loadJson("reference.json")
  } yield Data(phases, codeSize, generatedCodeSize, metrics, buildTime, backends, backendReference, annotations, reference)

def fileIndex() = loadJson("index.json").map(_.asInstanceOf[js.Array[String]].toSet)

def loadJsonByDate(interval: DateInterval)(name: String): Future[js.Array[js.Dynamic]] =
  val startYear = interval.start.getFullYear().toInt
  val endYear = interval.end.getFullYear().toInt
  val startMonth = interval.start.getMonth().toInt + 1
  val endMonth = interval.end.getMonth().toInt + 1

  def generateMonths(currentYear: Int, currentMonth: Int): List[(Int, Int)] =
    if (currentYear < endYear || (currentYear == endYear && currentMonth <= endMonth)) {
      val nextYear = if (currentMonth == 12) currentYear + 1 else currentYear
      val nextMonth = if (currentMonth < 12) currentMonth + 1 else 1
      (currentYear, currentMonth) :: generateMonths(nextYear, nextMonth)
    } else List()
  
  val months = generateMonths(startYear, startMonth)
  Future.sequence {
    months.map { case (year, month) =>
      val fileName = s"$name/${year}" + "%02d".format(month) + ".json"
      fileIndex().flatMap { index =>
        if (index.contains(fileName)) loadJson(fileName)
        else Future.successful(js.Array())
      }
    }
  }.map(_.reduce((arr1, arr2) => arr1.concat(arr2)))

var fileCache = HashMap[String, Future[js.Array[js.Dynamic]]]()
def loadJson(file: String): Future[js.Array[js.Dynamic]] = fileCache.synchronized {
  fileCache.getOrElseUpdate(file, {
    Ajax.get(s"data/$file").map { xhr =>
      val result = js.JSON.parse(xhr.responseText).asInstanceOf[js.Array[js.Dynamic]]
      result
    }
  })
}

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
    BackendsTime(filtered, "llvm").draw(),
    BackendsTime(filtered, "js").draw(),
    BackendsMemory(filtered, "llvm").draw(),
    BackendsMemory(filtered, "js").draw(),
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

def renderReferenceSection(referenceData: js.Array[js.Dynamic], backendReference: js.Array[js.Dynamic])(implicit C: AnnotationContext): HtmlElement = {
  val latest = backendReference.last // TODO: make sure that the data is sorted
  val merged = js.Object.assign(js.Object(), latest.asInstanceOf[js.Object], referenceData.asInstanceOf[js.Object])

  val languages = js.Object.keys(merged)
  val benchmarks = languages.flatMap(key => js.Object.keys(merged.asInstanceOf[js.Dynamic].selectDynamic(key).asInstanceOf[js.Object])).distinct

  sectionTag(
    benchmarks.map { benchmark =>
      Reference(benchmark, merged).draw()
    }
  )
}

def renderPlots(normalize: Boolean, dateInterval: DateInterval): Future[HtmlElement] = allDataInRange(dateInterval).map { allData =>
  given C: AnnotationContext = new AnnotationContext(normalize, allData.annotations)

  val preprocessor = new TimePreprocessor((date: js.Date) => {
    date.getTime > dateInterval.start.getTime && date.getTime < dateInterval.end.getTime
  })

  val phasesData = preprocessor.filter(allData.phases)
  val codeSizeData = preprocessor.filter(allData.codeSize)
  val generatedCodeSizeData = preprocessor.filter(allData.generatedCodeSize)
  val buildTimeData = preprocessor.filter(allData.buildTime)
  val metricsData = preprocessor.filter(allData.metrics)
  val backendsData = preprocessor.filter(allData.backends)
  val backendReferenceData = preprocessor.filter(allData.backendReference)

  // too much filtering, nothing left
  if (phasesData.isEmpty) sectionTag()
  else sectionTag(
    h2("Build Performance", flexBasis.percent(100)),
    p("The time per phase is extracted using the Effekt `--time json` flag.", flexBasis.percent(100)),
    trackedPhaseDirectories.map { (dir: String) => renderPhaseSection(dir, phasesData) },
    h2("Performance over Time", flexBasis.percent(100)),
    trackedBenchmarks.map { (benchmarks: String) => renderBackendsSection(benchmarks, backendsData) },
    h2("Relative Performance", flexBasis.percent(100)),
    renderReferenceSection(allData.reference, backendReferenceData),
    h2("Compiler Build Performance", flexBasis.percent(100)),
    p("The metrics are gathered by measuring `effekt -b <file>` using `gnutime`. Therefore, these metrics include the overhead of JVM.", flexBasis.percent(100)),
    renderMetricsSection(metricsData),
    h2("General Metrics", flexBasis.percent(100)),
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

  val normalize = Var(false)

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
          val rangeStart = new js.Date(startDate.now())
          val rangeEnd = new js.Date(endDate.now())

          // set to start and end of day
          rangeStart.setUTCHours(0, 0, 0, 0)
          rangeEnd.setUTCHours(23, 59, 59, 999)

          renderPlots(normalize.now(), DateInterval(rangeStart, rangeEnd)).map(renderBus.emit)
        }
      ),
    ),
    div(
      className := "control",
      button("toggle normalization", onClick --> {_ => normalize.update(!_) }),
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
