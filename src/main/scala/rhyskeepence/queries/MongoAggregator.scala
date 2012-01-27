package rhyskeepence.queries

import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.Duration
import rhyskeepence.Clock
import org.scala_tools.time.Imports._


trait MongoAggregator {
  val clock = new Clock

  val sumReduction = """
    function (name, values) {
      var result = {
        aggregate: 0
      };

      values.forEach(function(f) {
        result.aggregate += f.aggregate;
      });

      return result;
    }
    """

  def aggregate(environment: String, metricName: String, duration: Duration): List[DBObject]

  def getLabel(environment: String, metricName: String) = metricName + " (" + environment + ")"

  def findNewerThan(duration: Duration) = {
    val timeLimit = (clock.now - duration).toDateMidnight.getMillis
    Some(MongoDBObject("_id" -> MongoDBObject("$gt" -> timeLimit)))
  }
}