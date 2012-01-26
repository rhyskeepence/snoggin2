package rhyskeepence.snippet

import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.S
import java.lang.String
import com.mongodb.casbah.Imports._
import com.mongodb.BasicDBObject
import net.liftweb.common.Full
import rhyskeepence.queries.{FiveMinuteAverage, ErrorsPerDay, AveragePerDay, CountPerDay}
import org.joda.time.Duration

class Stats {

  def render = {
    val fields = S.param("fields").map(_.split(",").toList).openOr(List[String]())

    val aggregator = S.param("aggregate") match {
      case Full("count") => new CountPerDay
      case Full("average") => new AveragePerDay
      case Full("errors") => new ErrorsPerDay
      case _ => new FiveMinuteAverage
    }

    val duration = S.param("days") match {
      case Full(days) => Duration.standardDays(days.toLong)
      case _ => Duration.standardDays(7)
    }

    val allStats = fields.map {
      field =>
        val Array(environment, metricName) = field.split(":")

        val allPoints = aggregator.aggregate(environment, metricName, duration)

        val dataPoints = allPoints.map {
          dbObject =>
            val timestamp = dbObject.getAsOrElse[Double]("_id", 0)
            val value = dbObject.get("value") match {
              case b: BasicDBObject => b.getAsOrElse[Double]("aggregate", 0)
              case _ => 0
            }

            JsArray(timestamp, value)
        }

        JsObj(
          ("data", JsArray(dataPoints.toList)),
          ("label", Str(aggregator.getLabel(environment, metricName) + " = 0")))

    }

    Script(
      Call("doPlot", JsArray(allStats))
    )
  }
}