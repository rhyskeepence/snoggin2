package rhyskeepence.legacyadaptor

import org.scala_tools.time.Imports._
import collection.mutable.ListBuffer
import rhyskeepence.Clock
import java.io.File

class StatisticFileSource(collectionDirectory: String, clock: Clock) {

  val yymmddPattern = DateTimeFormat.forPattern("yyyyMMdd")

  def filesFor(duration: Period): List[StatisticFile] = {
    generateFileNames(clock.now - duration).toList
  }

  private def generateFileNames(dateToCheck: DateTime, files: ListBuffer[StatisticFile] = ListBuffer()): ListBuffer[StatisticFile] = {
    val statisticFilePath = statisticFilePathFor(dateToCheck)
    if (statisticFilePath.exists()) {
      files ++= collectCsvFilesIn(statisticFilePath)
    }

    if (dateToCheck + 1.day >= clock.midnight)
      files
    else
      generateFileNames(dateToCheck + 1.day, files)
  }

  private def statisticFilePathFor(dateToCheck: DateTime) = {
    val yyyymmdd = yymmddPattern.print(dateToCheck)
    new File(collectionDirectory, yyyymmdd)
  }
  
  private def collectCsvFilesIn(statisticFilePath: File) = {
    statisticFilePath
      .listFiles
      .filter( file => file.getName.matches("[\\w\\-]+\\.csv") )
      .map( file => StatisticFile(file))
  }
}


