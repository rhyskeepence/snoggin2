package rhyskeepence.queries.mongo

import rhyskeepence.queries.AggregatorFactory
import rhyskeepence.caching.Cacheable

class MongoAggregatorFactory extends AggregatorFactory {
  def sumPerDay = new SumPerDay with Cacheable
  def averagePerDay = new AveragePerDay with Cacheable
  def dailyThroughput = new DailyThroughput with Cacheable
  def errorsPerDay = new ErrorsPerDay with Cacheable
  def noAggregation = new HighResolutionAverage with Cacheable
}
