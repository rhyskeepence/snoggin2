package rhyskeepence.queries

import org.joda.time.Duration
import rhyskeepence.caching.Cacheable

trait AverageAggregator extends MongoAggregator with MongoQuery with Cacheable {

  private def map(metricName: String, millisecondsPerBucket: Long) =
    "function() { emit(this._id - (this._id % " + millisecondsPerBucket + "), { aggregate:0, count:1, sum: this." + metricName + " } );}"

  private val reduceFunc = """
    function (name, values) {
      var result = { aggregate:0, count:0, sum:0 };
      values.forEach(function(f) {
        if (f.sum > -1) {
          result.sum += f.sum;
          result.count += f.count;
        }
      });
      return result;
    }
    """

  private val averageFunc = """
    function (who, res) {
      if (res.count > 0) res.aggregate = res.sum / res.count;
      return res;
    }
    """

  def aggregate(millisecondsPerBucket: Long, environment: String, metricName: String, duration: Duration) = {
    val cacheKey = "average-%s-%s-%s-%s".format(millisecondsPerBucket, environment, metricName, duration.getStandardSeconds)

    getCachedOrUpdate(cacheKey) {
      dataPointStore.mapReduce(
        environment,
        findNewerThan(duration),
        map(metricName, millisecondsPerBucket),
        reduceFunc,
        Some(averageFunc))
    }


  }
}