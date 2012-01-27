package rhyskeepence.legacyadaptor

import org.joda.time.DateTime
import akka.actor._
import akka.actor.Actor._
import java.util.concurrent.TimeUnit
import rhyskeepence.storage.{MongoDataPointStore, MongoStorage}
import net.liftweb.util.Props
import org.scala_tools.time.Imports._
import rhyskeepence.Clock

class Loader extends Actor {

  var latestContentProcessed: Option[DateTime] = None
  private lazy val collectionDirectory = Props.get("loader.directory", ".")

  val clock = new Clock
  val mongoStore = new MongoDataPointStore(new MongoStorage)
  val dataPointSource = new FileToDataPointAdaptor(
    new StatisticFileSource(collectionDirectory, clock),
    new CsvStatisticsFileParser
  )

  def receive = {
    case "poll" =>
      val processFrom = mongoStore.lastModified.toDateMidnight.plusDays(1)
      println("Loader: reading content modified since " + processFrom)
      dataPointSource.processDataPointsFor(new Period(processFrom, clock.now)) {
        dataPoints =>
          println("Loader: inserting " + dataPoints.size + " data points...")
          mongoStore write dataPoints
          println("Loader: done inserting")
      }
  }
}

object Loader {
  val loader = actorOf[Loader].start()
  val clock = new Clock

  val oneDay = 1000 * 60 * 60 * 24
  val initialLoadAtOneAm = Props.getBool("only.load.at.oneam", true)


  def start() = {
    val durationTillStart =
      if (initialLoadAtOneAm) {

        val startTime = if (clock.midnight.plusHours(1) < clock.now)
          clock.midnight.plusDays(1).plusHours(1)
        else
          clock.midnight.plusHours(1)

        new Duration(clock.now, startTime)
      } else {
        new Duration(0)
      }

    println("Loader: scheduling next load at " + (clock.now + durationTillStart))
    Scheduler.schedule(loader, "poll", durationTillStart.getMillis, oneDay, TimeUnit.MILLISECONDS)
  }

  def shutdown() {
    Scheduler.shutdown()
  }
}