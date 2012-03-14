package rhyskeepence.queries.mongo

import org.joda.time.Interval

class AveragePerDay extends AverageAggregator {

  val oneDay = 86400000;

  override def aggregate(environment: String, metricName: String, duration: Interval) = {
    aggregate(oneDay, environment, metricName, duration)
  }
}