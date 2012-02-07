package rhyskeepence.queries

import org.joda.time.Duration
import rhyskeepence.caching.Cacheable

class SumPerDay extends MongoAggregator with MongoQuery with Cacheable {

  def mapByDayValuesGreaterThanZero(metricName: String) =
    "function() { " +
      "  var metric = this." + metricName + "; " +
      "  if (metric > 0) emit(this._id - (this._id % 86400000), { aggregate: metric } );" +
      "}"

  override def aggregate(environment: String, metricName: String, duration: Duration) = {
    val cacheKey = "sumperday-%s-%s-%s".format(environment, metricName, duration.getStandardSeconds)
    getCachedOrGenerate(cacheKey) {
      dataPointStore.mapReduce(environment, findNewerThan(duration), mapByDayValuesGreaterThanZero(metricName), sumReduction, None)
    }
  }
}