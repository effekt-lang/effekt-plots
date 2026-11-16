package utils

import scalajs.js
import org.scalajs.dom
import scala.collection.mutable.Map

case class Annotation(id: Int, date: js.Date, reason: String)

class AnnotationContext(val normalized: Boolean, annotationData: js.Array[AnnotationJson], val commits: js.Array[CommitJson]) {
  val annotations: js.Array[Annotation] = annotationData.zipWithIndex.map { case (annotation, index) =>
    Annotation(index + 1, new js.Date(annotation.date), annotation.reason)
  }

  val annotationClickHandlers = Map[(Int, Int), js.Function1[dom.MouseEvent, Unit]]()
}
