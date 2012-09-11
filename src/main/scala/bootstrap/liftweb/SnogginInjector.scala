package bootstrap.liftweb

import snoggin.Clock
import snoggin.caching.SnogginCache
import snoggin.storage.{MongoDataPointStore, MongoStorage}
import net.liftweb.http.Factory
import snoggin.queries.mongo.MongoAggregatorFactory
import snoggin.queries.AggregatorFactory

object SnogginInjector extends Factory {

  implicit object clock extends FactoryMaker(() => clockInstance)
  private val clockInstance = new Clock

  implicit object aggregatorFactory extends FactoryMaker[AggregatorFactory](() => aggregatorFactoryInstance)
  private val aggregatorFactoryInstance = new MongoAggregatorFactory

  implicit object mongoStore extends FactoryMaker(() => mongoStoreInstance)
  private val mongoStoreInstance = new MongoDataPointStore(new MongoStorage)

  implicit object cache extends FactoryMaker(() => cacheInstance)
  private val cacheInstance = new SnogginCache

}
