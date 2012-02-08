package rhyskeepence.queries

import com.mongodb.DBObject
import org.joda.time.Duration

trait Aggregator {

  def aggregate(environment: String, metricName: String, duration: Duration): List[DBObject]

  def getLabel(environment: String, metricName: String) = metricName + " (" + environment + ")"

}

