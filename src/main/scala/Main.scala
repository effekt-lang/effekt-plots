import data._
import org.scalajs.dom
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom.XMLHttpRequest
import scalajs.js

// TODO: this should be async
def loadData(name: String): js.Dynamic = 
  val xhr = XMLHttpRequest()
  xhr.open("get", s"data/$name.json", false)
  xhr.send(null)
  js.JSON.parse(xhr.responseText)

@main
def main(): Unit =
  val codeSizeData = loadData("cloc").asInstanceOf[js.Array[js.Dynamic]]
  val codeSizePlot = codeSize(codeSizeData).draw()
  renderOnDomContentLoaded(dom.document.body, codeSizePlot)
