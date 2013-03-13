package snoggin

import scala._
import Stats._

case class Stats(data: Seq[(StatName, Map[Timestamp, StatValue])]) {
  def keys = data.map(_._1)
  def values = data.map(_._2)
  def isEmpty = data.isEmpty
}

object Stats {
  type StatName = String
  type Timestamp = Double
  type StatValue = Double

  implicit def seqToStats(statSeq: Seq[(StatName, Map[Timestamp, StatValue])]) = new Stats(statSeq)
}
