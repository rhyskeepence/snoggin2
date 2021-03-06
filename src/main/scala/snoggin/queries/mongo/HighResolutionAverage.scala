package snoggin.queries.mongo

import org.joda.time.Interval

class HighResolutionAverage extends MaxAggregator {

  override def aggregate(environment: String, metricName: String, interval: Interval) = {
    val sizeOfEachBucket = 300000 // 5 minute buckets for longer charts
    aggregate(sizeOfEachBucket, environment, metricName, interval)
  }

  def aggregatorName = "high-res-average"
}