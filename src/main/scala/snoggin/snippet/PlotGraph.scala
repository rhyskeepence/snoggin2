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
import net.liftweb.http.js.JE.Call
import net.liftweb.common.Full
import net.liftweb.http.js.JE.Str
import collection.immutable.Iterable
import net.liftweb.http.js.JsObj
import net.liftweb.http.js.JE.JsObj

class PlotGraph {
  val clock = SnogginInjector.clock.vend
  val aggregatorFactory = SnogginInjector.aggregatorFactory.vend
  val dateFormat = StaticDateTimeFormat.forPattern("dd-MM-yyyy")
  type Stats = Map[String, Map[Double, Double]]

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


    val stats: Stats = fields
    .map(splitEnvironmentAndMetric)
    .map { case (environment, metricName) =>
      aggregator.getLabel(environment, metricName) -> aggregator.aggregate(environment, metricName, chartPeriod).map(dbObjectToTuple).toMap
    }
    .toMap

    val groupedStats: Map[String, Map[Double, Double]] = S.param("semigroup") match {
      case Full("product") => {
        val key = stats.keys.mkString(" multiplied by ")
        Map(key -> combineMapValues(stats, multiply))
      }
      case Full("divide") => {
        val key = stats.keys.mkString(" divided by ")
        Map(key -> combineMapValues(stats, divide))
      }
      case _ => stats
    }

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

  def combineMapValues(stats: Stats, semigroup: (Double, Double) => Double): Map[Double, Double] = {
    stats.values.reduceLeft {
      (seed, next) => next.map {
        case (timestamp, value) =>
          val seedValue = seed.get(timestamp).getOrElse(1.0)
          (timestamp, semigroup(seedValue, value))
      }
    }
  }


  def multiply(a: Double, b: Double): Double = {
    a * b
  }

  def divide(a: Double, b: Double): Double = {
    if (b != 0)
      a / b
    else
      0
  }

  private def dbObjectToTuple: DBObject => (Double,Double) = { dbObject =>
    val timestamp = dbObject.getAsOrElse[Double]("_id", 0)
    val value = dbObject.get("value") match {
      case b: BasicDBObject => b.getAsOrElse[Double]("aggregate", 0)
      case _ => 0
    }
    (timestamp, value)
  }
  
  private def splitEnvironmentAndMetric: String => (String,String) = { field =>
    field.split(":") match {
      case Array(environment, metric) => (environment, metric)
      case _ => sys.error("malformed field: %s".format(field))
    }
  }

  private def intervalStartingFrom(period: Period) = new Interval(period, clock.now)
}