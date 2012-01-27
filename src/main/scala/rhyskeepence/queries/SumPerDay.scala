package rhyskeepence.queries

import org.joda.time.Duration

class SumPerDay extends MongoAggregator with MongoQuery {

  def mapByDayValuesGreaterThanZero(metricName: String) =
    "function() { " +
      "  var metric = this." + metricName + "; " +
      "  if (metric > 0) emit(this._id - (this._id % 86400000), { aggregate: metric } );" +
      "}"

  override def aggregate(environment: String, metricName: String, duration: Duration) = {
    dataPointStore.mapReduce(environment, findNewerThan(duration), mapByDayValuesGreaterThanZero(metricName), sumReduction, None)
  }
}