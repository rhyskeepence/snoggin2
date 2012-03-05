package rhyskeepence.storage

import rhyskeepence.model.DataPoint
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.query.Imports._
import scala.collection.JavaConverters._
import com.mongodb.casbah.commons.{MongoDBObjectBuilder, MongoDBObject}
import com.mongodb.casbah.map_reduce.MapReduceInlineOutput
import scala.collection.immutable.List

class MongoDataPointStore(mongoStorage: MongoStorage) {

  def write(dataPoint: DataPoint) {
    withCollection(dataPoint.environment) {
      dbCollection =>
        dbCollection += dataPointToMongoObject(dataPoint).result
    }
  }

  def mapReduce(environment: String, query: Option[DBObject], map: JSFunction, reduce: JSFunction, finalizeFunction: Option[JSFunction]) = {
    withCollection(environment) {
      _.mapReduce(map, reduce, MapReduceInlineOutput, query, None, None, finalizeFunction).toList
    }
  }

  def environmentNames = {
    mongoStorage.collectionNames
      .toList
      .filter(!_.startsWith("system"))
      .filter(_ != "updates")
      .map(_.replaceAll("_", "-"))
  }

  def metricNamesFor(environment: String) = {
    withCollection(environment) {
      _.find()
        .sort(MongoDBObject("_id" -> -1))
        .limit(1)
        .toList
        .headOption
        .map(mongoObj => mongoObj.keySet().asScala.toList)
        .getOrElse(List[String]())
        .filter(_ != "_id")
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
    val legalCollectionName = environment replaceAll("\\-", "_")
    mongoStorage.withCollection(legalCollectionName)(doWithContent)
  }

}