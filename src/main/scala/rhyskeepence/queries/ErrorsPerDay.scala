package rhyskeepence.queries

import org.joda.time.Duration
import rhyskeepence.caching.Cacheable

class ErrorsPerDay extends MongoAggregator with MongoQuery with Cacheable {

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
    val cacheKey = "errors-%s-%s-%s".format(environment, metricName, duration.getStandardSeconds)

    getCachedOrUpdate(cacheKey) {
      dataPointStore.mapReduce(environment, findNewerThan(duration), mapByDayIf10SecondOutage(metricName), sumReduction, Some(convertToMinutes))
    }
  }

  override def getLabel(environment: String, metricName: String) = {
    environment + " - downtime (minutes)"
  }
}