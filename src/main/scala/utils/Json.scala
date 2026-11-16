package utils

import scala.scalajs.js

type Fields[A] = js.Dictionary[A]

trait Meta extends js.Object {
  val commit: String
  val commitDate: String
  val currentDate: String
}

trait TimedPoint extends js.Object {
  val meta: Meta
}

trait PhasePoint extends TimedPoint {
  val total: Fields[Double]
  val `type`: js.UndefOr[String]
}

trait BuildPoint extends TimedPoint {
  val buildTime: String
}

trait Metric extends js.Object {
  val maxMem: String
  val userTime: String
  val cpuUsage: String
}

trait BackendMetric extends js.Object {
  val maxMem: js.UndefOr[Double]
  val time: Double
  val arg: Double
}

trait SizeMetric extends js.Object {
  val code: Double
}

trait ReferenceMetric extends js.Object {
  val mean: js.UndefOr[Double]
  val stddev: js.UndefOr[Double]
  val arg: Double
}

trait AnnotationJson extends js.Object {
  val date: String
  val reason: String
}

trait CommitJson extends js.Object {
  val hash: String
  val date: Double
  val message: String
}

object Json {
  def fields[A](value: js.Object): Fields[A] = value.asInstanceOf[Fields[A]]

  def keys[A](value: Fields[A]): js.Array[String] = js.Object.keys(value.asInstanceOf[js.Object])

  def seriesKeys[P <: js.Object, A](points: js.Array[P], field: P => Fields[A]): js.Array[String] =
    val seen = scala.collection.mutable.HashSet[String]()
    val result = js.Array[String]()
    points.foreach(point => keys(field(point)).foreach { key =>
      if (seen.add(key)) result.push(key)
    })
    result

  def date(point: TimedPoint): js.Date = new js.Date(point.meta.currentDate.toDouble * 1000)
}
