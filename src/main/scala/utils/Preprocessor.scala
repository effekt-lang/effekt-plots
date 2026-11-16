package utils

import scalajs.js

trait Preprocessor {
  def filter[T <: TimedPoint](data: js.Array[T]): js.Array[T]
}

class TimePreprocessor(matches: js.Date => Boolean) extends Preprocessor {
  def filter[T <: TimedPoint](data: js.Array[T]) = data.filter(point => matches(Json.date(point)))
}

class SubstitutionPreprocessor(includeKey: String => Boolean, renameKey: String => String) extends Preprocessor {
  private def filteredFields(value: js.Object): js.Object =
    js.Object.fromEntries(
      js.Object.entries(Json.fields[js.Any](value))
        .filter { case js.Tuple2(key, _) => includeKey(key) }
        .map { case js.Tuple2(key, fieldValue) => js.Tuple2(renameKey(key), fieldValue) }
    ).asInstanceOf[js.Object]

  def filter[T <: TimedPoint](data: js.Array[T]): js.Array[T] =
    data.map { point =>
      val pointFields = Json.fields[js.Object](point)
      js.Object.fromEntries(
        js.Object.keys(point).filter(_ != "type").map { fieldName =>
          val fieldValue = if (fieldName == "meta") point.meta else filteredFields(pointFields(fieldName))
          js.Tuple2(fieldName, fieldValue)
        }
      ).asInstanceOf[T]
    }
}
