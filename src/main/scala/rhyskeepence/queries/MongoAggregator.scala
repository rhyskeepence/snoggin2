package rhyskeepence.queries

import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject

trait MongoAggregator {
  def aggregate(metricName: String): List[DBObject]

  def getLabel(fieldName: String) = fieldName

  def query(metricName: String) = {
    Some(MongoDBObject("metric" -> metricName))
  }
}