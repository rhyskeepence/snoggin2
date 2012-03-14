package rhyskeepence.caching

import rhyskeepence.queries.Aggregator
import bootstrap.liftweb.SnogginInjector
import org.joda.time.Interval
import org.scala_tools.time.Imports._
import com.mongodb.DBObject

trait Cacheable extends Aggregator {
  val cache = SnogginInjector.cache.vend

  abstract override def aggregate(environment: String, metricName: String, interval: Interval) = {
    Multiplexer.multiplex(interval) {
      singleDay =>
        getAggregation(environment, metricName, singleDay)
    }
  }

  def getAggregation(environment: String, metricName: String, interval: Interval): scala.List[DBObject] = {
    val cacheKey = cacheKeyFor(environment, metricName, interval)
    val fromCache = cache.get(cacheKey)
    fromCache.getOrElse {
      val newValue = super.aggregate(environment, metricName, interval)
      cache.put(cacheKey, newValue)
      newValue
    }
  }

  def cacheKeyFor(environment: String, metricName: String, interval: Interval) = {
    "aggregation-%s-%s-%s-%s-%s".format(
      getClass.getName, environment, metricName, interval.start.millis, interval.duration.millis)
  }
}

object Multiplexer  {
  def multiplex[T](interval: Interval)(f: Interval => List[T]): List[T] = {
    splitIntervalIntoSingleDays(interval).flatMap(f)
  }

  private def splitIntervalIntoSingleDays(interval: Interval, result: List[Interval] = List()): List[Interval] = {
    val start = interval.end.toDateMidnight
    val end = interval.end

    val resultWithAddedInterval = new Interval(start, end) :: result

    if (interval.contains(start)) {
      splitIntervalIntoSingleDays(new Interval(interval.start, start.toDateTime.minusMillis(1)), resultWithAddedInterval)
    } else {
      resultWithAddedInterval
    }
  }
}