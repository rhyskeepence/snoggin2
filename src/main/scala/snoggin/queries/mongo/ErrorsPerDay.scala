package snoggin.queries.mongo

import org.joda.time.Interval

class ErrorsPerDay extends MongoAggregator with MongoQuery {

  def mapByDayIf10SecondOutage(metricName: String) =
    "function() { " +
      "  var metric = this['" + metricName + "']; " +
      // each -1 is a 10 second outage
      "  if (metric == -1) emit(this._id - (this._id % 86400000), { aggregate: 10 } );" +
      "  else emit(this._id - (this._id % 86400000), { aggregate: 0 } );" +
      "}"

  val convertToMinutes = """
    function (who, res) {
      res.aggregate = res.aggregate / 60;
      return res;
    }
  """

  def aggregate(environment: String, metricName: String, interval: Interval) = {
    dataPointStore.mapReduce(
      environment,
      aggregateWithin(interval),
      mapByDayIf10SecondOutage(metricName),
      sumReduction,
      Some(convertToMinutes))
  }

  override def getLabel(environment: String, metricName: String) = {
    environment + " - downtime (minutes)"
  }

  def aggregatorName = "errors-per-day"
}