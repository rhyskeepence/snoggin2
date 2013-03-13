package snoggin.restful

import snoggin.Stats
import snoggin.Stats._
import org.scala_tools.time.Imports._

object CsvRenderer {
  def renderAsCsv(stats: Stats) = {
    val values = stats.values.toList
    val allTimestamps = values
      .map(_.keySet)
      .foldLeft(Set[Timestamp]())(_ union _)
      .toList.sorted

    val timestampValues = allTimestamps.map(timestamp => timestamp -> values.map(_ get timestamp getOrElse 0.0))

    renderHeaderRow(stats) + renderDataRows(timestampValues)
  }

  def renderHeaderRow(stats: Stats) = {
    if (stats.isEmpty)
      "No data"
    else
      "timestamp," + stats.keys.toList.mkString(",") + "\n"
  }

  def renderDataRows(timestampValues: List[(Timestamp, List[StatValue])]) = {
    timestampValues.map {
      case (timestamp, values) => renderTimestamp(timestamp) + "," + values.mkString(",")
    }.mkString("\n")
  }

  def renderTimestamp(timestamp: Double) = {
    new DateTime(timestamp.toLong).toString("yyyy-MM-dd HH:mm:ss.SSS")
  }
}
