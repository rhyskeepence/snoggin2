package rhyskeepence.queries

import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.Duration
import rhyskeepence.Clock
import org.scala_tools.time.Imports._


trait MongoAggregator {
  val clock = new Clock

  def aggregate(environment: String, metricName: String, duration: Duration): List[DBObject]

  def getLabel(environment: String, metricName: String) = metricName + " (" + environment + ")"

  def query(environment: String, duration: Duration) = {
    val timeLimit = (clock.now - duration).getMillis
    Some(MongoDBObject("env" -> environment, "time" -> MongoDBObject("$gt" -> timeLimit)))
  }
}