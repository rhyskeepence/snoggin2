package rhyskeepence.queries

trait AggregatorFactory {
  def sumPerDay: Aggregator
  def averagePerDay: Aggregator
  def dailyThroughput: Aggregator
  def errorsPerDay: Aggregator
  def noAggregation: Aggregator
}