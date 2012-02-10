package rhyskeepence.queries.mongo

class FindApplicationMetric extends MongoQuery {
                                                 
  def applicationNames = dataPointStore.environmentNames

  def metricNamesFor(application: String) = {
    dataPointStore.metricNamesFor(application)
  }
}
