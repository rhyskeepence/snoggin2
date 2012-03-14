package rhyskeepence.queries.mongo

import org.joda.time.Interval
import org.scala_tools.time.Imports._
import com.mongodb.casbah.Imports._
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

  def aggregateWithin(interval: Interval) = {
    Some(("_id" $lt interval.end.millis $gt interval.start.millis))
  }
}