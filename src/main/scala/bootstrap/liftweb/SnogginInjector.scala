package bootstrap.liftweb

import rhyskeepence.Clock
import rhyskeepence.caching.SnogginCache
import rhyskeepence.storage.{MongoDataPointStore, MongoStorage}
import net.liftweb.http.Factory
import rhyskeepence.queries.mongo.{FindApplicationMetric, MongoAggregatorFactory}

object SnogginInjector extends Factory {

  implicit object clock extends FactoryMaker(() => clockInstance)
  private val clockInstance = new Clock

  implicit object aggregatorFactory extends FactoryMaker(() => aggregatorFactoryInstance)
  private val aggregatorFactoryInstance = new MongoAggregatorFactory

  implicit object mongoStore extends FactoryMaker(() => mongoStoreInstance)
  private val mongoStoreInstance = new MongoDataPointStore(new MongoStorage)

  implicit object findApplicationMetric extends FactoryMaker(() => findApplicationMetricInstance)
  private val findApplicationMetricInstance = new FindApplicationMetric

  implicit object cache extends FactoryMaker(() => cacheInstance)
  private val cacheInstance = new SnogginCache

}
