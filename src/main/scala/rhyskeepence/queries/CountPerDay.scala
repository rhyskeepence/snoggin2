package rhyskeepence.queries

import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.Duration


class CountPerDay extends MongoAggregator with MongoQuery {

  def mapFunc(metricName: String) =
    "function() { emit(this.time - (this.time % 86400000), this." + metricName + ".floatApprox);}"

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

  override def aggregate(environment: String, metricName: String, duration: Duration) = {
    dataPointStore.mapReduce(query(environment, duration), mapFunc(metricName), reduceFunc, None)
  }
}