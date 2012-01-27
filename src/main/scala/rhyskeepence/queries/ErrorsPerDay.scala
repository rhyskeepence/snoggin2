package rhyskeepence.queries

import org.joda.time.Duration

class ErrorsPerDay extends MongoAggregator with MongoQuery {

  def mapByDayIf10SecondOutage(metricName: String) =
    "function() { " +
      "  var metric = this." + metricName + "; " +
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

  override def aggregate(environment: String, metricName: String, duration: Duration) = {
    dataPointStore.mapReduce(environment, findNewerThan(duration), mapByDayIf10SecondOutage(metricName), sumReduction, Some(convertToMinutes))
  }

  override def getLabel(environment: String, metricName: String) = {
    environment + " - downtime (minutes)"
  }
}