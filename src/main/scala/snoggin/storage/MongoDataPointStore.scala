package snoggin.storage

import com.mongodb.casbah.MongoCollection
import scala.collection.JavaConverters._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.map_reduce.MapReduceInlineOutput
import com.mongodb.casbah.commons.Imports._
import snoggin.model.DataPoint

class MongoDataPointStore(mongoStorage: MongoStorage) {

  def write(dataPoint: DataPoint) {
    withCollection(dataPoint.environment) {
      dbCollection =>
        dbCollection += dataPointToMongoObject(dataPoint)
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
        .limit(100)
        .flatMap(mongoObj => mongoObj.keySet().asScala)
        .toSet
        .filter(_ != "_id")
        .toList.sorted
    }
  }

  private def dataPointToMongoObject: (DataPoint) => DBObject = item => {
    val contentItemBuilder = MongoDBObject.newBuilder
    contentItemBuilder += "_id" -> item.timestamp

    item.metrics.foreach {
      metric =>
        contentItemBuilder += metric.name -> metric.value
    }

    contentItemBuilder.result()
  }

  private def withCollection[T](environment: String)(doWithContent: MongoCollection => T) = {
    val legalCollectionName = environment replaceAll("\\-", "_")
    mongoStorage.withCollection(legalCollectionName)(doWithContent)
  }

}