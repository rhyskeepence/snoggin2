package snoggin.queries

import com.mongodb.DBObject
import org.joda.time.Interval

trait Aggregator {

  def aggregate(environment: String, metricName: String, duration: Interval): List[DBObject]

  def getLabel(environment: String, metricName: String) = metricName + " (" + environment + ")"

  def aggregatorName: String
}

