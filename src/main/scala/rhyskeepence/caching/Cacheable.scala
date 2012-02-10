package rhyskeepence.caching

import rhyskeepence.queries.Aggregator
import org.joda.time.Duration
import bootstrap.liftweb.SnogginInjector

trait Cacheable extends Aggregator {
  val cache = SnogginInjector.cache.vend

  abstract override def aggregate(environment: String, metricName: String, duration: Duration) = {
    val cacheKey = "aggregation-%s-%s-%s-%s".format(getClass.getName, environment, metricName, duration.getStandardSeconds)
    val fromCache = cache.get(cacheKey)
    fromCache.getOrElse {
      val newValue = super.aggregate(environment, metricName, duration)
      cache.put(cacheKey, newValue)
      newValue
    }
  }
}