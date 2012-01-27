package rhyskeepence.queries

import org.joda.time.Duration

class AveragePerDay extends AverageAggregator {

  val oneDay = 86400000;

  override def aggregate(environment: String, metricName: String, duration: Duration) = {
    aggregate(oneDay, environment, metricName, duration)
  }
}