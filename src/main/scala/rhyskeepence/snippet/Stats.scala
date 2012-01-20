package rhyskeepence.snippet

import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.S
import java.lang.String
import com.mongodb.casbah.Imports._
import net.liftweb.common.Full
import rhyskeepence.queries.{ErrorsPerDay, AveragePerDay, CountPerDay}

class Stats {

  def render = {
    val fields = S.param("fields").map(_.split(",").toList).openOr(List[String]())

    val aggregator = S.param("aggregate") match {
      case Full("average") => new AveragePerDay
      case Full("errors") => new ErrorsPerDay
      case _ => new CountPerDay
    }

    val allStats = fields.map {
      fieldName =>

        val allPoints = aggregator.aggregate(fieldName)

        val dataPoints = allPoints.map {
          dbObject =>
            JsArray(
              dbObject.getAsOrElse[Double]("_id", 0),
              dbObject.getAs[BasicDBObject]("value").get.getAsOrElse[Double]("aggregate", 0)
            )
        }

        JsObj(
          ("data", JsArray(dataPoints.toList)),
          ("label", Str(aggregator.getLabel(fieldName))))

    }

    Script(
      Call("doPlot", JsArray(allStats))
    )
  }
}