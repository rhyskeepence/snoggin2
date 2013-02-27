package snoggin.snippet

import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import com.mongodb.casbah.Imports._
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.JE.Str
import net.liftweb.http.js.JsObj
import net.liftweb.http.js.JE.JsObj
import xml.Node
import snoggin.StatsGeneration._
import snoggin.{StatsGeneration, StatsRequestParser}

class PlotGraph {
  val requestParser = new StatsRequestParser()

  def render() = {
    val statsRequest = requestParser.buildStatsRequest
    val stats = StatsGeneration.generateStats(statsRequest)
    renderAsJavascript(stats)
  }

  def renderAsJavascript(groupedStats: Stats): Node = {
    val jsDataPoints: List[JsObj] = groupedStats.toList.map {
      case (label, values) =>
        val javascriptValues = values.toList.sortBy(_._1).map {
          case (timestamp, value) => JsArray(timestamp, value)
        }

        JsObj(
          ("data", JsArray(javascriptValues)),
          ("label", Str(label + " = 0")))
    }

    if (jsDataPoints.isEmpty)
      Script(Call("notifyNoStats"))
    else
      Script(Call("doPlot", JsArray(jsDataPoints)))
  }
}