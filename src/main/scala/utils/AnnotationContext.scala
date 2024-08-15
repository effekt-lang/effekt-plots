package utils

import scalajs.js
import scala.collection.mutable.Map

case class Annotation(id: Int, date: js.Date, reason: String)

class AnnotationContext(val _annotations: js.Array[js.Dynamic]) {
    var annotationCount = 0

    val annotations: js.Array[Annotation] = _annotations.map { annotation =>
        annotationCount += 1
        Annotation(annotationCount, new js.Date(annotation.date.asInstanceOf[String]), annotation.reason.asInstanceOf[String])
    }

    val handlers = Map[(Int, Int), js.Function1[js.Dynamic, Unit]]()
}
