package snoggin.snippet

import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.S
import com.mongodb.casbah.Imports._
import com.mongodb.BasicDBObject
import net.liftweb.common.Full
import bootstrap.liftweb.SnogginInjector
import org.joda.time.{DateTime, Interval, Period}
import org.scala_tools.time.StaticDateTimeFormat
import org.scala_tools.time.Imports._

class PlotGraph {
  val clock = SnogginInjector.clock.vend
  val aggregatorFactory = SnogginInjector.aggregatorFactory.vend
  val dateFormat = StaticDateTimeFormat.forPattern("dd-MM-yyyy")

  def render = {
    val fields =
      S.param("fields")
        .map(_.split(",").toList.filter(!_.isEmpty))
        .openOr(List())

    val optionalDateRange = for {
      fromDate <- S.param("fromDate").flatMap(dateFormat.parseOption(_))
      toDate <- S.param("toDate").flatMap(dateFormat.parseOption(_))
    } yield new Interval(fromDate.plusDays(1), toDate.plusDays(1))

    val daysToChart = S.param("days").map(_.toInt) match {
      case Full(days) => Period.days(days)
      case _ => Period.days(7)
    }

    val chartPeriod = optionalDateRange.getOrElse(intervalStartingFrom(daysToChart))

    val aggregator = S.param("aggregate") match {
      case Full("sum") => aggregatorFactory.sumPerDay
      case Full("average") => aggregatorFactory.averagePerDay
      case Full("dailythroughput") => aggregatorFactory.dailyThroughput
      case Full("errors") => aggregatorFactory.errorsPerDay
      case _ =>
        if (chartPeriod.duration > 1.day.standardDuration) aggregatorFactory.noAggregationMultipleDays
        else aggregatorFactory.noAggregationOneDay
    }

    val allStats = fields.map {
      field =>
        val (environment, metricName) = splitEnvironmentAndMetric(field)
        val mongoObjects = aggregator.aggregate(environment, metricName, chartPeriod)
        val dataPoints = mongoObjects.map(dbObjectToJavascript)

        JsObj(
          ("data", JsArray(dataPoints)),
          ("label", Str(aggregator.getLabel(environment, metricName) + " = 0")))
    }

    if (allStats.isEmpty) 
      Script(Call("notifyNoStats"))
    else
      Script(Call("doPlot", JsArray(allStats)))
  }

  private def dbObjectToJavascript: DBObject => JsArray = { dbObject =>
    val timestamp = dbObject.getAsOrElse[Double]("_id", 0)
    val value = dbObject.get("value") match {
      case b: BasicDBObject => b.getAsOrElse[Double]("aggregate", 0)
      case _ => 0
    }

    JsArray(timestamp, value)
  }
  
  private def splitEnvironmentAndMetric(field: String) = {
    field.split(":") match {
      case Array(environment, metric) => (environment, metric)
      case _ => sys.error("malformed field: %s".format(field))
    }
  }

  private def intervalStartingFrom(period: Period) = new Interval(period, clock.now)
}