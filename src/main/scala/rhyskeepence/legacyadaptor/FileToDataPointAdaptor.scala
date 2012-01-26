package rhyskeepence.legacyadaptor

import org.joda.time.Period
import rhyskeepence.model.DataPoint

class FileToDataPointAdaptor(source: StatisticFileSource, fileParser: CsvStatisticsFileParser) {

  def processDataPointsFor(period: Period)(process: List[DataPoint] => Unit) {
    val files = source.filesFor(period)
    files.foreach { file =>
      println("Reading " + file)
      process( fileParser.getDataPointsFrom(file) )
    }
  }
}