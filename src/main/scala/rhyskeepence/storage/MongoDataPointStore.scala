package rhyskeepence.storage

import rhyskeepence.model.DataPoint
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.commons.{MongoDBObjectBuilder, MongoDBObject}
import com.mongodb.casbah.map_reduce.MapReduceStandardOutput
import org.joda.time.DateTime


class MongoDataPointStore(mongoStorage: MongoStorage) {

  def write(items: List[DataPoint]) {
    withContent {
      dataPointCollection =>

        dataPointCollection.ensureIndex("env")

        items.foreach {
          dataPointCollection += dataPointToMongoObject(_).result
        }
    }
  }

  def mapReduce(query: Option[DBObject], map: JSFunction, reduce: JSFunction, finalizeFunction: Option[JSFunction]) = {
    withContent {
      contentCollection =>
        contentCollection.mapReduce(map, reduce, MapReduceStandardOutput("map-reduce-output"), query, None, None, finalizeFunction)
        mongoStorage.withCollection("map-reduce-output") {
          _.find().toList
        }
    }
  }

  def mostRecentLastModified = {
    withContent {

      _.find()
        .sort(MongoDBObject("time" -> -1))
        .limit(1)
        .toList
        .headOption
        .map( _.get("time").asInstanceOf[Long] )
        .map( new DateTime(_) )
        .getOrElse( new DateTime().minusDays(90) )

    }
  }

  private def dataPointToMongoObject: (DataPoint) => MongoDBObjectBuilder = {
    item =>
      val contentItemBuilder = MongoDBObject.newBuilder
      contentItemBuilder += "env" -> item.environment
      contentItemBuilder += "time" -> item.timestamp

      item.metrics.foreach {
        metric =>
          contentItemBuilder += metric.name -> metric.value
      }

      contentItemBuilder
  }

  private def withContent[T](doWithContent: MongoCollection => T) = {
    mongoStorage.withCollection("datapoints")(doWithContent)
  }

}