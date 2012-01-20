package rhyskeepence.queries

class ErrorsPerDay extends MongoAggregator with MongoQuery {

  val mapFunc = """
    function() {
      emit(this.timestamp - (this.timestamp % 86400000), this.value.floatApprox);
    }
    """

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

  def aggregate(metricName: String) = {
    dataPointStore.mapReduce(query(metricName), mapFunc, reduceFunc, Some(finalizeFunc))
  }

  override def getLabel(fieldName: String) = {
    fieldName.split("-").take(2).mkString(" ") + " - downtime (minutes)"
  }
}