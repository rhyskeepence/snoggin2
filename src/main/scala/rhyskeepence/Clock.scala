package rhyskeepence

import org.scala_tools.time.Imports._

class Clock {
  def now = DateTime.now
  def midnight = DateTime.now.toDateMidnight.toDateTime
}