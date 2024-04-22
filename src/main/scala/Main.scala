import data._
import org.scalajs.dom
import com.raquo.laminar.api.L.{*, given}

@main
def main(): Unit =
  println("Hello world!")
  val codeSizePlot = codeSize.draw()
  renderOnDomContentLoaded(dom.document.body, codeSizePlot)
