package rhyskeepence.legacyadaptor

import org.scala_tools.time.Imports._
import collection.mutable.ListBuffer
import rhyskeepence.Clock
import java.io.File

class StatisticFileSource(collectionDirectory: String, clock: Clock) {

  val yymmddPattern = DateTimeFormat.forPattern("yyyyMMdd")

  def filesSince(startingDate: DateTime): List[StatisticFile] = {
    generateFileNames(startingDate).toList
  }

  private def generateFileNames(dateToCheck: DateTime, files: ListBuffer[StatisticFile] = ListBuffer()): ListBuffer[StatisticFile] = {
    if (isToday(dateToCheck))
      return files

    val statisticFilePath = statisticFilePathFor(dateToCheck)
    if (statisticFilePath.exists()) {
      files ++= collectCsvFilesIn(statisticFilePath)
    }

    generateFileNames(dateToCheck + 1.day, files)
  }

  private def isToday(dateToCheck: DateTime) = dateToCheck >= clock.midnight

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


