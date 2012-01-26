package rhyskeepence.queries

import org.joda.time.Duration

class AveragePerDay extends MongoAggregator with MongoQuery {

  def mapFunc(metricName: String) =
    "function() { emit(this.time - (this.time % 86400000), this." + metricName + ".floatApprox);}"

  val reduceFunc = """
    function (name, values) {
      var sum = 0;
      var count = 0;
      values.forEach(function(f) {
        if (f > -1) {
          sum += f;
          count++;
        }
      });
      return {sum: sum, count: count};
    }
    """

  val finalizeFunc = """
    function (who, res) {
      if (res.count > 0) {
        res.aggregate = res.sum / res.count;
      } else {
        res.aggregate = 0;
      }
      return res;
    }
  """

  override def aggregate(environment: String, metricName: String, duration: Duration) = {
    dataPointStore.mapReduce(query(environment, duration), mapFunc(metricName), reduceFunc, Some(finalizeFunc))
  }
}