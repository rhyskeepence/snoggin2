package rhyskeepence.legacyadaptor

import org.joda.time.Period
import org.scala_tools.time.Imports._
import rhyskeepence.storage.{MongoStorage, MongoDataPointStore}

class FileToDataPointAdaptor(source: StatisticFileSource, fileParser: CsvStatisticsFileParser) {

  def getDataPointsFor(period: Period) = {
    val files = source.filesFor(period)
    files.flatMap ( fileParser.getDataPointsFrom(_) )
  }
}

object FileToDataPointAdaptor extends App {

  val dataPointSource = new FileToDataPointAdaptor(
    new StatisticFileSource("/Users/dev/tmp/production", "production-knitware-diamondquartz"),
    new CsvStatisticsFileParser("production-knitware")
  )
  val mongoStore = new MongoDataPointStore(new MongoStorage)

  val points = dataPointSource.getDataPointsFor(28.days)
  mongoStore.write(points)

}