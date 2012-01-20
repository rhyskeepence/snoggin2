package rhyskeepence.queries

import com.mongodb.casbah.commons.MongoDBObject


class CountPerDay extends MongoAggregator with MongoQuery {

  val mapFunc = """
    function() {
      emit(this.timestamp - (this.timestamp % 86400000), this.value.floatApprox);
    }
    """

  val reduceFunc = """
    function (name, values) {
      var sum = 0;
      values.forEach(function(f) {
        if (f > -1) {
          sum += f;
        }
      });
      return {aggregate: sum};
    }
    """

  def aggregate(metricName: String) = {
    dataPointStore.mapReduce(query(metricName), mapFunc, reduceFunc, None)
  }
}