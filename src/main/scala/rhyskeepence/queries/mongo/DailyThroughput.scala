package rhyskeepence.queries.mongo

import org.joda.time.Interval

class DailyThroughput extends MongoAggregator with MongoQuery {

  def mapByDay(metricName: String) =
    "function() { emit(this._id - (this._id % 86400000), { aggregate: this." + metricName + " * 10 } );}"

  override def aggregate(environment: String, metricName: String, interval: Interval) = {
    dataPointStore.mapReduce(
      environment,
      aggregateWithin(interval),
      mapByDay(metricName),
      sumReduction,
      None)
  }

  def getType = "daily-throughput"
}