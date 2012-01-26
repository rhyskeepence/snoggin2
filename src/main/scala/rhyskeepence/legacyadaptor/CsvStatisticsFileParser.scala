package rhyskeepence.legacyadaptor

import rhyskeepence.model.{Metric, DataPoint}

class CsvStatisticsFileParser {

  def getDataPointsFrom(statsFile: StatisticFile) = {
    val source = statsFile.source
    val csvData = source.getLines().map(_.split(","))

    val headerCsvRow = csvData.next()
    val dataCsvRows = csvData.drop(1)

    val dataPoints = dataCsvRows.map {
      row =>
        val rowTimestamp = extractTimestamp(row)
        val dataColumns = headerCsvRow.zip(row).drop(1)

        val metrics = dataColumns.map {
          headerAndData =>
            Metric(
              headerAndData._1,
              headerAndData._2.toDouble.toLong)
        }

        DataPoint(rowTimestamp, statsFile.environment, metrics.toList)
    }

    dataPoints.toList
  }

  private def extractTimestamp(row: Array[String]): Long = row(0).toLong
}