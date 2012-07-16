package rhyskeepence.queries.mongo

import org.joda.time.Interval

class HighResolutionAverage extends AverageAggregator {

  override def aggregate(environment: String, metricName: String, interval: Interval) = {

    val sizeOfEachBucket =
      if (interval.toDurationMillis < 86399999)
        30000  // 30 second buckets for less than 24 hrs (same day)
      else
        300000 // 5 minute buckets for 24 hr charts

    aggregate(sizeOfEachBucket, environment, metricName, interval)
  }

  def aggregatorName = "high-res-average"
}