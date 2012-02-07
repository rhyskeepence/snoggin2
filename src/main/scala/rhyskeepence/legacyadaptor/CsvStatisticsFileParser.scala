package rhyskeepence.legacyadaptor

import rhyskeepence.model.{Metric, DataPoint}

class CsvStatisticsFileParser {

  def getDataPointsFrom(statsFile: StatisticFile) = {
    val source = statsFile.source
    val csvData = source.getLines().map(_.split(","))

    val headerCsvRow = csvData.next()

    val dataPoints = csvData.map {
      row =>
        val rowTimestamp = extractTimestamp(row)
        val metrics = mapRowToMetrics(headerCsvRow, row)
        DataPoint(rowTimestamp, statsFile.environment, metrics.toList)
    }

    dataPoints.toList
  }

  private def mapRowToMetrics(headerRow: Array[String], row: Array[String]) = {
    val dataColumns = headerRow.zip(row).drop(1)

    dataColumns.map {
      case (name, value) =>
        Metric(name, value.toDouble)
    }
  }

  private def extractTimestamp(row: Array[String]): Long = row(0).toLong
}