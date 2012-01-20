package rhyskeepence.queries

import com.mongodb.casbah.commons.MongoDBObject


class AveragePerDay extends MongoAggregator with MongoQuery {

  val mapFunc = """
    function() {
      emit(this.timestamp - (this.timestamp % 86400000), this.value.floatApprox);
    }
    """

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
      res.aggregate = res.sum / res.count;
      return res;
    }
  """

  def aggregate(metricName: String) = {
    dataPointStore.mapReduce(query(metricName), mapFunc, reduceFunc, Some(finalizeFunc))
  }
}