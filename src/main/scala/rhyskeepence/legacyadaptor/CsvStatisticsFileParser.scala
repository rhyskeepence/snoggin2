package rhyskeepence.legacyadaptor

import io.Source
import rhyskeepence.model.DataPoint

class CsvStatisticsFileParser(environment: String) {

  def getDataPointsFrom(source: Source) = {
    val csvData = source.getLines().map(_.split(",")).toList

    val headerRow = csvData.head
    val dataRows = csvData.drop(1)

    dataRows.flatMap {
      dataRow =>
        val rowTimestamp = extractTimestamp(dataRow)
        val dataColumns = headerRow.zip(dataRow).drop(1)

        dataColumns.map(
          headerAndData =>
            DataPoint(rowTimestamp, headerName(headerAndData._1), headerAndData._2.toDouble.toLong)
        )
    }
  }
  
  private def headerName(columnName: String) = environment + "-" + columnName

  private def extractTimestamp(row: Array[String]): Long = row(0).toLong
}