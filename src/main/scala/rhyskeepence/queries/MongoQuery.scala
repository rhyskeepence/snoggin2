package rhyskeepence.queries

import rhyskeepence.storage.{MongoStorage, MongoDataPointStore}


trait MongoQuery {
  val collection = "datapointmongos"
  val dataPointStore = new MongoDataPointStore(new MongoStorage)
}