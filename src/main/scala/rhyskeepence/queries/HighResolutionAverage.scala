package rhyskeepence.queries

import org.joda.time.Duration

class HighResolutionAverage extends AverageAggregator {

  override def aggregate(environment: String, metricName: String, duration: Duration) = {
    val sizeOfEachBucket = duration.getMillis / 1500
    aggregate(sizeOfEachBucket, environment, metricName, duration)
  }

}