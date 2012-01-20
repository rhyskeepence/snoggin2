package rhyskeepence.storage

import rhyskeepence.model.DataPoint
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.commons.{MongoDBObjectBuilder, MongoDBObject}
import com.mongodb.casbah.map_reduce.MapReduceStandardOutput


class MongoDataPointStore(mongoStorage: MongoStorage) {

  def write(items: List[DataPoint]) {
    withContent {
      dataPointCollection =>

        dataPointCollection.ensureIndex("metric")

        items
          .map(dataPointToMongoObject)
          .foreach { dataPointCollection += _.result }

    }
  }

  def mapReduce(query: Option[DBObject], map: JSFunction, reduce: JSFunction, finalizeFunction: Option[JSFunction]) = {
    withContent {
      contentCollection =>
        contentCollection.mapReduce(map, reduce, MapReduceStandardOutput("map-reduce-output1"), query, None, None, finalizeFunction)
        mongoStorage.withCollection("map-reduce-output1") {
          _.find().toList
        }
    }
  }

  private def dataPointToMongoObject: (DataPoint) => MongoDBObjectBuilder = {
    item =>
      val contentItemBuilder = MongoDBObject.newBuilder
      contentItemBuilder += "metric" -> item.metric
      contentItemBuilder += "timestamp" -> item.timestamp
      contentItemBuilder += "value" -> item.value
      contentItemBuilder
  }

  private def withContent[T](doWithContent: MongoCollection => T) = {
    mongoStorage.withCollection("datapoints")(doWithContent)
  }

}