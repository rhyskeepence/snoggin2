package rhyskeepence.caching

import rhyskeepence.queries.Aggregator
import bootstrap.liftweb.SnogginInjector
import com.mongodb.DBObject
import org.joda.time.{DateTimeZone, Interval}

trait Cacheable extends Aggregator {
  val cache = SnogginInjector.cache.vend

  abstract override def aggregate(environment: String, metricName: String, interval: Interval) = {
    Multiplexer.multiplexDaily(interval) {
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
      aggregatorName, environment, metricName, interval.getStart.getMillis, interval.toDuration.getMillis)
  }
}

object Multiplexer  {
  def multiplexDaily[T](interval: Interval)(f: Interval => List[T]): List[T] = {
    splitIntervalIntoSingleDays(interval).flatMap(f)
  }

  private def splitIntervalIntoSingleDays(interval: Interval, result: List[Interval] = List()): List[Interval] = {
    val intervalEndInUtc = interval.getEnd.toDateTime(DateTimeZone.UTC)
    val start = intervalEndInUtc.toDateMidnight.toDateTime
    val end = intervalEndInUtc

    val resultWithAddedInterval = new Interval(start, end) :: result

    val endOfNextInterval = start.minusMillis(1)
    if (interval.contains(endOfNextInterval)) {
      splitIntervalIntoSingleDays(new Interval(interval.getStart, endOfNextInterval), resultWithAddedInterval)
    } else {
      resultWithAddedInterval
    }
  }
}