package rhyskeepence.queries.mongo

import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.Duration
import org.scala_tools.time.Imports._
import rhyskeepence.queries.Aggregator
import bootstrap.liftweb.SnogginInjector

trait MongoAggregator extends Aggregator {
  val clock = SnogginInjector.clock.vend

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

  def findNewerThan(duration: Duration) = {
    val timeLimit = (clock.now - duration).toDateMidnight.plusDays(1).getMillis
    Some(MongoDBObject("_id" -> MongoDBObject("$gt" -> timeLimit)))
  }
}