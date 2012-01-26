package rhyskeepence.queries

import org.joda.time.Duration

class ErrorsPerDay extends MongoAggregator with MongoQuery {

  def mapFunc(metricName: String) =
    "function() { emit(this.time - (this.time % 86400000), this." + metricName + ".floatApprox);}"


  val reduceFunc = """
    function (name, values) {
      var count = 0;
      values.forEach(function(f) {
        if (f == -1) {
          count = count + 5; // 5 seconds per -1 blip
        }
      });
      return {count: count};
    }
    """

  val finalizeFunc = """
    function (who, res) {
      res.aggregate = res.count / 60;  // report downtime in minutes
      return res;
    }
  """

  override def aggregate(environment: String, metricName: String, duration: Duration) = {
    dataPointStore.mapReduce(query(environment, duration), mapFunc(metricName), reduceFunc, Some(finalizeFunc))
  }

  override def getLabel(environment: String, metricName: String) = {
    environment + " - downtime (minutes)"
  }
}