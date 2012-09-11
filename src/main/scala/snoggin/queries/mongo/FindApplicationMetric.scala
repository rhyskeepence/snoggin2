package snoggin.queries.mongo

import bootstrap.liftweb.SnogginInjector

class FindApplicationMetric extends MongoQuery {
  val cache = SnogginInjector.cache.vend

  def applicationNames = dataPointStore.environmentNames

  def metricNamesFor(application: String) = {
    val cacheKey = "metric-names-" + application
    cache.get(cacheKey).getOrElse {
      val metrics = dataPointStore.metricNamesFor(application)
      cache.putShortLived(cacheKey, metrics)
      metrics
    }
  }
}
