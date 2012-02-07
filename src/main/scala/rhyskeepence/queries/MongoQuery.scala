package rhyskeepence.queries

import rhyskeepence.storage.{MongoStorage, MongoDataPointStore}

trait MongoQuery {
  val dataPointStore = new MongoDataPointStore(new MongoStorage)
}