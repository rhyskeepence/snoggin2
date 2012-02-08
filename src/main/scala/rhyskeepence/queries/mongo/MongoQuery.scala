package rhyskeepence.queries.mongo

import bootstrap.liftweb.SnogginInjector

trait MongoQuery {
  val dataPointStore = SnogginInjector.mongoStore.vend
}