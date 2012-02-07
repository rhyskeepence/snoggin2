package rhyskeepence.storage

import rhyskeepence.model.DataPoint
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.commons.{MongoDBObjectBuilder, MongoDBObject}
import org.joda.time.DateTime
import collection.immutable.List
import java.lang.String
import com.mongodb.casbah.map_reduce.MapReduceInlineOutput

class MongoDataPointStore(mongoStorage: MongoStorage) {

  def write(items: List[DataPoint]) {
    val itemsGroupedByEnvironment = items groupBy (_.environment) mapValues (_.map(_.environment))

    itemsGroupedByEnvironment.foreach {
      case (environment, dataPoints) =>

        withCollection(environment) {
          dbCollection =>

            items.foreach {
              dbCollection += dataPointToMongoObject(_).result
            }
        }
    }

    updateLastModified(items)
  }

  def mapReduce(environment: String, query: Option[DBObject], map: JSFunction, reduce: JSFunction, finalizeFunction: Option[JSFunction]) = {
    withCollection(environment) {
        _.mapReduce(map, reduce, MapReduceInlineOutput, query, None, None, finalizeFunction).toList
    }
  }

  def lastModified = {
    withCollection("updates") {
      _.find()
        .sort(MongoDBObject("timestamp" -> -1))
        .limit(1)
        .toList
        .headOption
        .map(_.get("timestamp").asInstanceOf[Long])
        .map(new DateTime(_))
        .getOrElse(new DateTime().minusDays(90))
    }
  }

  private def updateLastModified(items: List[DataPoint]) {
    val maxTimestamp = items.foldLeft(0L)((i, m) => m.timestamp.max(i))
    withCollection("updates") {
      val lastModifiedUpdate = MongoDBObject.newBuilder += "timestamp" -> maxTimestamp
      _ += lastModifiedUpdate.result
    }
  }

  private def dataPointToMongoObject: (DataPoint) => MongoDBObjectBuilder = {
    item =>
      val contentItemBuilder = MongoDBObject.newBuilder
      contentItemBuilder += "_id" -> item.timestamp

      item.metrics.foreach {
        metric =>
          contentItemBuilder += metric.name -> metric.value
      }

      contentItemBuilder
  }

  private def withCollection[T](environment: String)(doWithContent: MongoCollection => T) = {
    val legalCollectionName = environment replaceAll ("\\-", "_")
    mongoStorage.withCollection(legalCollectionName)(doWithContent)
  }

}