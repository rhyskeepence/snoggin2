package snoggin

import net.liftweb.http.S
import org.joda.time.Period
import bootstrap.liftweb.SnogginInjector
import org.scala_tools.time.StaticDateTimeFormat
import org.scala_tools.time.Imports._
import net.liftweb.common.Full

object StatsRequestParser {
  val clock = SnogginInjector.clock.vend
  val aggregatorFactory = SnogginInjector.aggregatorFactory.vend
  val dateFormat = StaticDateTimeFormat.forPattern("dd-MM-yyyy")

  def buildStatsRequest = {
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

    val semigroup = S.param("semigroup")

    StatsRequest(fields, aggregator, chartPeriod, semigroup)
  }

  private def intervalStartingFrom(period: Period) = new Interval(period, clock.now)
}