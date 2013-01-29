package snoggin.queries.mongo

import org.joda.time.Interval

trait MaxAggregator extends MongoAggregator with MongoQuery {

  private def map(metricName: String, millisecondsPerBucket: Long) =
    "function() { emit(this._id - (this._id % " + millisecondsPerBucket + "), { aggregate: this['" + metricName + "'] } );}"

  private val reduceFunc = """
    function (name, values) {
      var result = { aggregate:0 };
      values.forEach(function(f) {
        if (f.aggregate != null && f.aggregate > result.aggregate) {
          result.aggregate = f.aggregate;
        }
      });
      return result;
    }
    """

  def aggregate(millisecondsPerBucket: Long, environment: String, metricName: String, interval: Interval) = {
      dataPointStore.mapReduce(
        environment,
        aggregateWithin(interval),
        map(metricName, millisecondsPerBucket),
        reduceFunc,
        None)
  }
}