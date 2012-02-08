package rhyskeepence.legacyadaptor

import rhyskeepence.model.DataPoint
import net.liftweb.common.Logger
import org.joda.time.DateTime

class FileToDataPointAdaptor(source: StatisticFileSource, fileParser: CsvStatisticsFileParser) extends Logger {

  def processDataPointsSince[T](startingDate: DateTime)(process: List[DataPoint] => T) {
    source.filesSince(startingDate).foreach { file =>
      info("Reading " + file)
      process( fileParser.getDataPointsFrom(file) )
    }
  }
}