import data._
import utils._
import org.scalajs.dom
import com.raquo.laminar.api.L.{*, given}
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.collection.mutable.HashMap
import dom.ext.Ajax
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import scalajs.js

val trackedPhaseDirectories = js.Array(
  "examples/casestudies/",
)

val trackedBenchmarks = js.Array(
  "are_we_fast_yet",
  "duality_of_compilation",
  "effect_handlers_bench",
  "input_output",
  "folklore_to_fact",
)

val trackedIndividualBuild = js.Array(
  "examples/casestudies/prettyprinter.effekt.md"
)

case class DateInterval(start: js.Date, end: js.Date)

case class PlotData(
  phases: js.Array[PhasePoint],
  codeSize: js.Array[TimedPoint],
  generatedCodeSize: js.Array[TimedPoint],
  metrics: js.Array[TimedPoint],
  buildTime: js.Array[BuildPoint],
  backends: js.Array[TimedPoint],
  backendReference: js.Array[TimedPoint],
  annotations: js.Array[AnnotationJson],
  reference: Fields[Fields[ReferenceMetric]],
  commits: js.Array[CommitJson],
)

def loadPlotData(interval: DateInterval): Future[PlotData] =
  val phases = loadJsonByDate[PhasePoint](interval, "phases")
  val codeSize = loadJsonByDate[TimedPoint](interval, "cloc")
  val generatedCodeSize = loadJsonByDate[TimedPoint](interval, "out_loc")
  val metrics = loadJsonByDate[TimedPoint](interval, "metrics")
  val buildTime = loadJsonByDate[BuildPoint](interval, "build")
  val backends = loadJsonByDate[TimedPoint](interval, "backends")
  val backendReference = loadJsonByDate[TimedPoint](interval, "reference")
  val annotations = loadJson[js.Array[AnnotationJson]]("annotations.json")
  val reference = loadJson[Fields[Fields[ReferenceMetric]]]("reference.json")
  val commits = loadJson[js.Array[CommitJson]]("commits.json")
  for {
    phasePoints <- phases
    codeSizePoints <- codeSize
    generatedCodeSizePoints <- generatedCodeSize
    metricPoints <- metrics
    buildPoints <- buildTime
    backendPoints <- backends
    backendReferencePoints <- backendReference
    annotationData <- annotations
    referenceResults <- reference
    commitData <- commits
  } yield PlotData(
    phasePoints, codeSizePoints, generatedCodeSizePoints, metricPoints, buildPoints,
    backendPoints, backendReferencePoints, annotationData, referenceResults, commitData
  )

def loadFileIndex() = loadJson[js.Array[String]]("index.json").map(_.toSet)

def loadJsonByDate[T <: TimedPoint](interval: DateInterval, directory: String): Future[js.Array[T]] =
  val startYear = interval.start.getFullYear().toInt
  val endYear = interval.end.getFullYear().toInt
  val startMonth = interval.start.getMonth().toInt + 1
  val endMonth = interval.end.getMonth().toInt + 1

  def monthsInRange(currentYear: Int, currentMonth: Int): List[(Int, Int)] =
    if (currentYear < endYear || (currentYear == endYear && currentMonth <= endMonth)) {
      val nextYear = if (currentMonth == 12) currentYear + 1 else currentYear
      val nextMonth = if (currentMonth < 12) currentMonth + 1 else 1
      (currentYear, currentMonth) :: monthsInRange(nextYear, nextMonth)
    } else List()
  
  val months = monthsInRange(startYear, startMonth)
  loadFileIndex().flatMap { availableFiles =>
    val monthlyLoads = months.map { case (year, month) =>
      val fileName = f"$directory/$year$month%02d.json"
      if (availableFiles.contains(fileName)) loadJson[js.Array[T]](fileName)
      else Future.successful(js.Array[T]())
    }
    Future.sequence(monthlyLoads).map(_.foldLeft(js.Array[T]())((points, month) => points.concat(month)))
  }

val jsonCache = HashMap[String, Future[js.Any]]()
def loadJson[T <: js.Any](fileName: String): Future[T] = jsonCache.synchronized {
  jsonCache.getOrElseUpdate(fileName, {
    Ajax.get(s"data/$fileName").map(response => js.JSON.parse(response.responseText))
  }).asInstanceOf[Future[T]]
}

def renderPhaseSection(directory: String, phasesData: js.Array[PhasePoint])(implicit annotationContext: AnnotationContext): HtmlElement = {
  val preprocessor = SubstitutionPreprocessor(
    includeKey = key => key.startsWith(directory) || key.startsWith("./" + directory),
    renameKey = key => key.replace("./" + directory, "").replace(directory, "")
  )
  val filteredPhases = preprocessor.filter(phasesData)

  sectionTag(
    h3(directory, flexBasis.percent(100)),
    PhaseTimes(filteredPhases).draw(),

    if (filteredPhases.exists(point => Json.keys(point.total).nonEmpty))
      PhaseTimesAccumulated(filteredPhases).draw()
    else
      div(),
  )
}

def renderBackendsSection(benchmarkGroup: String, backendsData: js.Array[TimedPoint])(implicit annotationContext: AnnotationContext): HtmlElement = {
  val preprocessor = SubstitutionPreprocessor(
    includeKey = _.startsWith(benchmarkGroup + "/"),
    renameKey = _.replace(benchmarkGroup + "/", "")
  )
  val filteredBackends = preprocessor.filter(backendsData)

  sectionTag(
    h3(benchmarkGroup, flexBasis.percent(100)),
    BackendsTime(filteredBackends, "llvm").draw(),
    BackendsTime(filteredBackends, "js").draw(),
    BackendsTime(filteredBackends, "chez-cps").draw(),
    BackendsMemory(filteredBackends, "llvm").draw(),
    BackendsMemory(filteredBackends, "js").draw(),
    BackendsMemory(filteredBackends, "chez-cps").draw(),
  )
}

def renderMetricsSection(metricsData: js.Array[TimedPoint])(implicit annotationContext: AnnotationContext): HtmlElement = {
  val filteredMetrics = metricsData.map { point =>
    js.Object.fromEntries(
      js.Object.entries(Json.fields[js.Any](point)).filter { case js.Tuple2(key, _) =>
        trackedIndividualBuild.contains(key) || trackedIndividualBuild.contains("./" + key) || key == "meta"
      }
    ).asInstanceOf[TimedPoint]
  }

  sectionTag(
    MemoryUsage(filteredMetrics).draw(),
    CompileTime(filteredMetrics).draw(),
    CpuUsage(filteredMetrics).draw(),
  )
}

def renderReferenceSection(referenceData: Fields[Fields[ReferenceMetric]], backendReference: js.Array[TimedPoint])(implicit annotationContext: AnnotationContext): HtmlElement = {
  if (backendReference.isEmpty) return div()

  val latestBackendResults = backendReference.last
  val resultsByLanguage = js.Object.assign(js.Object(), latestBackendResults, referenceData.asInstanceOf[js.Object]).asInstanceOf[Fields[Fields[ReferenceMetric]]]

  val languages = Json.keys(resultsByLanguage).filter(_ != "meta")
  val benchmarks = languages.flatMap(language => Json.keys(resultsByLanguage(language))).distinct

  sectionTag(
    benchmarks.map { benchmark =>
      Reference(benchmark, resultsByLanguage).draw()
    }
  )
}

def renderPlots(normalize: Boolean, dateInterval: DateInterval): Future[HtmlElement] = loadPlotData(dateInterval).map { plotData =>
  given annotationContext: AnnotationContext = new AnnotationContext(normalize, plotData.annotations, plotData.commits)

  val dateFilter = new TimePreprocessor((date: js.Date) => {
    date.getTime > dateInterval.start.getTime && date.getTime < dateInterval.end.getTime
  })

  val phasesData = dateFilter.filter(plotData.phases)
  val codeSizeData = dateFilter.filter(plotData.codeSize)
  val generatedCodeSizeData = dateFilter.filter(plotData.generatedCodeSize)
  val buildTimeData = dateFilter.filter(plotData.buildTime)
  val metricsData = dateFilter.filter(plotData.metrics)
  val backendsData = dateFilter.filter(plotData.backends)
  val backendReferenceData = dateFilter.filter(plotData.backendReference)

  sectionTag(
    h2("Build Performance", flexBasis.percent(100)),
    p("The time per phase is extracted using the Effekt `--time json` flag.", flexBasis.percent(100)),
    trackedPhaseDirectories.map(directory => renderPhaseSection(directory, phasesData)),
    h2("Performance over Time", flexBasis.percent(100)),
    trackedBenchmarks.map(benchmarkGroup => renderBackendsSection(benchmarkGroup, backendsData)),
    h2("Relative Performance", flexBasis.percent(100)),
    p("Using the latest data point within the selected range.", flexBasis.percent(100)),
    renderReferenceSection(plotData.reference, backendReferenceData),
    h2("Compiler Build Performance", flexBasis.percent(100)),
    p("The metrics are gathered by measuring `effekt -b <file>` using `gnutime`. Therefore, these metrics include the overhead of JVM.", flexBasis.percent(100)),
    renderMetricsSection(metricsData),
    h2("General Metrics", flexBasis.percent(100)),
    sectionTag(
      GeneratedCodeSize(generatedCodeSizeData, "llvm").draw(),
      GeneratedCodeSize(generatedCodeSizeData, "js").draw(),
      GeneratedCodeSize(generatedCodeSizeData, "chez-cps").draw(),
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
  var latestRequestId = 0
  val startDate = Var(nWeeksBack(4))
  val endDate = Var(new js.Date().toISOString().split('T')(0))

  val normalize = Var(false)

  def generatePlots(): Unit = {
    latestRequestId += 1
    val requestId = latestRequestId
    val logo = dom.document.querySelector(".header img")
    logo.classList.add("loading")
    val rangeStart = new js.Date(startDate.now())
    val rangeEnd = new js.Date(endDate.now())

    rangeStart.setUTCHours(0, 0, 0, 0)
    rangeEnd.setUTCHours(23, 59, 59, 999)

    renderPlots(normalize.now(), DateInterval(rangeStart, rangeEnd)).onComplete {
      case Success(plots) if requestId == latestRequestId =>
        logo.classList.remove("loading")
        renderBus.emit(plots)
      case Failure(error) if requestId == latestRequestId =>
        logo.classList.remove("loading")
        dom.console.error(error)
        renderBus.emit(div("Could not load plots: ", error.toString))
      case _ => ()
    }
  }

  div(
    onMountCallback(_ => generatePlots()),
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
        onClick --> { _ => generatePlots() }
      ),
    ),
    div(
      className := "control",
      button("toggle normalization", onClick --> {_ => normalize.update(!_) }),
      button("all time", onClick --> {_ => startDate.set("2024-04-01") }),
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
