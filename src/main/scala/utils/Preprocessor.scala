package utils

import scalajs.js

type TimeFilter = js.Date => Boolean

class Preprocessor(timeFilter: TimeFilter) {
    def filter(data: js.Array[js.Dynamic]) =
        data.filter { e =>
            val date = new js.Date(e.meta.currentDate.asInstanceOf[String].toDouble * 1000)
            timeFilter(date)
        }
}