package snoggin.queries.mongo

import org.joda.time.Interval
import com.mongodb.casbah.Imports._
import snoggin.queries.Aggregator
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
    Some(("_id" $lt interval.getEndMillis $gte interval.getStartMillis))
  }
}