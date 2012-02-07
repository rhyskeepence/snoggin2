package rhyskeepence.legacyadaptor

import org.joda.time.Period
import rhyskeepence.model.DataPoint
import net.liftweb.common.Logger

class FileToDataPointAdaptor(source: StatisticFileSource, fileParser: CsvStatisticsFileParser) extends Logger {

  def processDataPointsFor[T](period: Period)(process: List[DataPoint] => T) {
    source.filesFor(period).foreach { file =>
      info("Reading " + file)
      process( fileParser.getDataPointsFrom(file) )
    }
  }
}