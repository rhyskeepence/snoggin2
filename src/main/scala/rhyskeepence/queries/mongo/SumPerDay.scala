package rhyskeepence.queries.mongo

import org.joda.time.Interval

class SumPerDay extends MongoAggregator with MongoQuery {

  def mapByDayValuesGreaterThanZero(metricName: String) =
    "function() { " +
      "  var metric = this['" + metricName + "']; " +
      "  if (metric > 0) emit(this._id - (this._id % 86400000), { aggregate: metric } );" +
      "}"

  override def aggregate(environment: String, metricName: String, interval: Interval) = {
    dataPointStore.mapReduce(
      environment,
      aggregateWithin(interval),
      mapByDayValuesGreaterThanZero(metricName),
      sumReduction,
      None)
  }

  def aggregatorName = "sum-per-day"
}