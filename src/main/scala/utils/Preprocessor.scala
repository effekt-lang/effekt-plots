package utils

import scalajs.js

trait Preprocessor {
  def filter(data: js.Array[js.Dynamic]): js.Array[js.Dynamic]
}

class TimePreprocessor(matches: js.Date => Boolean) extends Preprocessor {
  def filter(data: js.Array[js.Dynamic]) =
    data.filter { e =>
      val date = new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000)
      matches(date)
    }
}

class SubstitutionPreprocessor(matches: String => Boolean, substitute: String => String) extends Preprocessor {
  def filter(data: js.Array[js.Dynamic]) =
    data.map { dyn =>
      js.Object.fromEntries(
        js.Object.keys(dyn.asInstanceOf[js.Object]).map { (str: String) =>
          js.Tuple2(
            str,
            js.Object.fromEntries(
              js.Object.entries(dyn.selectDynamic(str).asInstanceOf[js.Object]).filter { case js.Tuple2(key, value) =>
                matches(key) || str == "meta"
              }.map { case js.Tuple2(key, value) =>
                js.Tuple2(substitute(key), value)
              }
            )
          )
        }
      ).asInstanceOf[js.Dynamic]
    }
}