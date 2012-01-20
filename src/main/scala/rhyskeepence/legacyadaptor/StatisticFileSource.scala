package rhyskeepence.legacyadaptor

import org.scala_tools.time.Imports._
import io.Source
import java.io.File
import collection.mutable.ListBuffer

class StatisticFileSource(collectionDirectory: String, environment: String) {

  val yymmddPattern = DateTimeFormat.forPattern("yyyyMMdd")

  def filesFor(duration: Period): List[Source] = {
    generateFileNames(environment, duration.ago).toList
  }

  private def generateFileNames(environment: String, dateToCheck: DateTime, files: ListBuffer[Source] = ListBuffer()): ListBuffer[Source] = {
    val statisticFilePath = statisticFilePathFor(environment, dateToCheck)
    if (statisticFilePath.exists()) {
      files += Source.fromFile(statisticFilePath)
    }

    if (dateToCheck + 1.day > DateTime.now)
      files
    else
      generateFileNames(environment, dateToCheck + 1.day, files)
  }

  private def statisticFilePathFor(environment: String, dateToCheck: DateTime) = {
    val yyyymmdd = yymmddPattern.print(dateToCheck)
    new File(collectionDirectory, yyyymmdd + "/" + environment + "-" + yyyymmdd + ".csv")
  }
}