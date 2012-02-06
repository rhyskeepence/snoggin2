package rhyskeepence

import org.scala_tools.time.Imports._

class Clock {
  def now = DateTime.now

  def midnight = now.toDateMidnight.toDateTime

  def one_am = {
    if (midnight.plusHours(1) < now)
      midnight.plusDays(1).plusHours(1)
    else
      midnight.plusHours(1)
  }
}