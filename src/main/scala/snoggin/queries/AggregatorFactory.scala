package snoggin.queries

trait AggregatorFactory {
  def sumPerDay: Aggregator
  def averagePerDay: Aggregator
  def dailyThroughput: Aggregator
  def errorsPerDay: Aggregator
  def noAggregationOneDay: Aggregator
  def noAggregationMultipleDays: Aggregator
}