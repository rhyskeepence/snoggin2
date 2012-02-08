package rhyskeepence.snippet

import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.S
import com.mongodb.casbah.Imports._
import com.mongodb.BasicDBObject
import net.liftweb.common.Full
import org.joda.time.Duration
import rhyskeepence.queries._

class Stats {

  def render = {
    val fields =
      S.param("fields")
        .map(_.split(",").toList)
        .openOr(List[String]())

    val aggregator = S.param("aggregate") match {
      case Full("sum") => new SumPerDay
      case Full("average") => new AveragePerDay
      case Full("dailythroughput") => new DailyThroughput
      case Full("errors") => new ErrorsPerDay
      case _ => new HighResolutionAverage
    }

    val duration = S.param("days") match {
      case Full(days) => Duration.standardDays(days.toLong)
      case _ => Duration.standardDays(7)
    }

    val allStats = fields.map {
      field =>
        val Array(environment, metricName) = field.split(":")

        val mongoObjects = aggregator.aggregate(environment, metricName, duration)

        val dataPoints = mongoObjects.map {
          dbObject => toJsArray(dbObject)
        }

        JsObj(
          ("data", JsArray(dataPoints)),
          ("label", Str(aggregator.getLabel(environment, metricName) + " = 0")))
    }

    Script(Call("doPlot", JsArray(allStats)))
  }

  private def toJsArray(dbObject: DBObject) = {
    val timestamp = dbObject.getAsOrElse[Double]("_id", 0)
    val value = dbObject.get("value") match {
      case b: BasicDBObject => b.getAsOrElse[Double]("aggregate", 0)
      case _ => 0
    }

    JsArray(timestamp, value)
  }

}