package snoggin.queries.mongo

import org.joda.time.Interval

class SingleDayDetail extends MaxAggregator {
  override def aggregate(environment: String, metricName: String, interval: Interval) = {
    val sizeOfEachBucket = 30000 // 30 second bucket for less than 24 hrs (same day)
    aggregate(sizeOfEachBucket, environment, metricName, interval)
  }

  def aggregatorName = "single-day-detail"
}