package snoggin

import queries.Aggregator
import org.scala_tools.time.Imports._
import net.liftweb.common.Box

case class StatsRequest(fields: List[String], aggregator: Aggregator, chartPeriod: Interval, semigroup: Box[String])