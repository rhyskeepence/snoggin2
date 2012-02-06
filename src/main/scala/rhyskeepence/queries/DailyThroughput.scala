package rhyskeepence.queries

import org.joda.time.Duration
import rhyskeepence.caching.Cacheable

class DailyThroughput extends MongoAggregator with MongoQuery with Cacheable {

  def mapByDay(metricName: String) =
    "function() { emit(this._id - (this._id % 86400000), { aggregate: this." + metricName + " * 10 } );}"

  override def aggregate(environment: String, metricName: String, duration: Duration) = {
    val cacheKey = "dailythroughput-%s-%s-%s".format(environment, metricName, duration.getStandardSeconds)

    getCachedOrUpdate(cacheKey) {
      dataPointStore.mapReduce(
        environment,
        findNewerThan(duration),
        mapByDay(metricName),
        sumReduction,
        None)
    }
  }
}