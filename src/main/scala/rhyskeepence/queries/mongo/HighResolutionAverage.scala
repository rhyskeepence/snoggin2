package rhyskeepence.queries.mongo

import org.joda.time.Duration

class HighResolutionAverage extends AverageAggregator {

  override def aggregate(environment: String, metricName: String, duration: Duration) = {

    val sizeOfEachBucket =
      if (duration.getStandardSeconds > 86400)
        duration.getMillis / 1500
      else
        10000 // 10 second buckets for single day charts

    aggregate(sizeOfEachBucket, environment, metricName, duration)
  }

}