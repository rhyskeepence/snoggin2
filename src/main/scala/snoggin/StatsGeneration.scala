package snoggin

import com.mongodb.casbah.Imports._
import net.liftweb.common.Full
import com.mongodb.BasicDBObject

object StatsGeneration {
  type StatName = String
  type Timestamp = Double
  type StatValue = Double
  type Stats = Map[StatName, Map[Timestamp, StatValue]]

  def generateStats(statsRequest: StatsRequest): Stats = {
    val aggregator = statsRequest.aggregator

    val stats: Stats = statsRequest.fields
      .map(splitEnvironmentAndMetric)
      .map {
        case (environment, metricName) =>
          aggregator.getLabel(environment, metricName) -> aggregator.aggregate(environment, metricName, statsRequest.chartPeriod).map(dbObjectToTuple).toMap
      }.toMap

    statsRequest.semigroup match {
      case Full("multiply") => {
        val key = stats.keys.mkString(" multiplied by ")
        Map(key -> combineMapValues(stats, multiply))
      }
      case Full("divide") => {
        val key = stats.keys.mkString(" divided by ")
        Map(key -> combineMapValues(stats, divide))
      }
      case Full("add") => {
        val key = stats.keys.mkString(" plus ")
        Map(key -> combineMapValues(stats, add))
      }
      case Full("subtract") => {
        val key = stats.keys.mkString(" minus ")
        Map(key -> combineMapValues(stats, subtract))
      }
      case _ => stats
    }
  }

  private def splitEnvironmentAndMetric: String => (String,String) = { field =>
    field.split(":") match {
      case Array(environment, metric) => (environment, metric)
      case _ => sys.error("malformed field: %s".format(field))
    }
  }

  private def dbObjectToTuple: DBObject => (Timestamp,StatValue) = { dbObject =>
    val timestamp = dbObject.getAsOrElse[Double]("_id", 0)
    val value = dbObject.get("value") match {
      case b: BasicDBObject => b.getAsOrElse[Double]("aggregate", 0)
      case _ => 0
    }
    (timestamp, value)
  }

  private def combineMapValues(stats: Stats, semigroup: (StatValue, StatValue) => StatValue): Map[Timestamp, StatValue] = {
    stats.values.reduceLeft {
      (seed, next) => next.map {
        case (timestamp, value) =>
          val seedValue = seed.get(timestamp).getOrElse(1.0)
          (timestamp, semigroup(seedValue, value))
      }
    }
  }

  def multiply(a: StatValue, b: StatValue): StatValue = {
    a * b
  }

  def add(a: StatValue, b: StatValue): StatValue = {
    a + b
  }

  def subtract(a: StatValue, b: StatValue): StatValue = {
    a - b
  }

  def divide(a: StatValue, b: StatValue): StatValue = {
    if (b != 0)
      a / b
    else
      0
  }
}
