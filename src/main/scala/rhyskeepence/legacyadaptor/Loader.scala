package rhyskeepence.legacyadaptor

import akka.actor._
import akka.actor.Actor._
import java.util.concurrent.TimeUnit
import rhyskeepence.storage.{MongoDataPointStore, MongoStorage}
import net.liftweb.util.Props
import org.scala_tools.time.Imports._
import rhyskeepence.Clock
import net.liftweb.common.Logger
import rhyskeepence.caching.Cacheable

class Loader(mongoStore: MongoDataPointStore, dataPointSource: FileToDataPointAdaptor, clock: Clock) extends Actor with Logger with Cacheable {

  def receive = {
    case "load" =>
      val fromDate = mongoStore.lastModified.toDateMidnight.plusDays(1)
      info("Loader: reading content modified since " + fromDate)

      dataPointSource.processDataPointsFor(new Period(fromDate, clock.now)) {
        dataPoints =>
          info("Loader: inserting " + dataPoints.size + " data points...")
          mongoStore write dataPoints
          info("Loader: done inserting")
      }

      invalidateCache()
  }
}

object Loader extends Logger {

  val initialLoadAtOneAm = Props.getBool("only.load.at.oneam", true)
  val collectionDirectory = Props.get("loader.directory", ".")

  val clock = new Clock
  val mongoStore = new MongoDataPointStore(new MongoStorage)
  val dataPointSource = new FileToDataPointAdaptor(
    new StatisticFileSource(collectionDirectory, clock),
    new CsvStatisticsFileParser
  )

  def start() = {
    val durationTillStart = calculateDurationToFirstLoad
    info("Scheduling next load at " + (clock.now + durationTillStart))

    val contentLoadingActor = actorOf(new Loader(mongoStore, dataPointSource, clock)).start()

    Scheduler.schedule(
      contentLoadingActor,
      "load",
      durationTillStart.getMillis,
      Duration.standardDays(1).getMillis,
      TimeUnit.MILLISECONDS)

  }

  def shutdown() {
    Scheduler.shutdown()
  }

  private def calculateDurationToFirstLoad: Duration = {
    if (initialLoadAtOneAm)
      new Duration(clock.now, clock.one_am)
    else
      new Duration(0)
  }
}