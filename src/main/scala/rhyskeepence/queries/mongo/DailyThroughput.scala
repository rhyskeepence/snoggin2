package rhyskeepence.queries.mongo

import org.joda.time.Duration

class DailyThroughput extends MongoAggregator with MongoQuery {

  def mapByDay(metricName: String) =
    "function() { emit(this._id - (this._id % 86400000), { aggregate: this." + metricName + " * 10 } );}"

  override def aggregate(environment: String, metricName: String, duration: Duration) = {
    dataPointStore.mapReduce(
      environment,
      findNewerThan(duration),
      mapByDay(metricName),
      sumReduction,
      None)
  }
}