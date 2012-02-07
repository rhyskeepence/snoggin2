package rhyskeepence.legacyadaptor

import io.Source
import java.io.File

case class StatisticFile(file: File) {
  def source: Source = Source.fromFile(file)
  def environment = file.getName.split("-").drop(1).take(2).mkString("-")
}


