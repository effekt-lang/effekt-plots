package data

import plots.Line
import scala.scalajs.js

object codeSize extends Line {
  val y = js.Array(8, 22, 31, 36, 48, 17, 25)
  val x = y.zipWithIndex.map { case (_, i) => i }
  val test = 42
}