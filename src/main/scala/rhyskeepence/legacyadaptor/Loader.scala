package rhyskeepence.legacyadaptor

import akka.actor._
import akka.actor.Actor._
import akka.actor.Scheduler._
import java.util.concurrent.TimeUnit
import net.liftweb.util.Props
import net.liftweb.common.Logger
import org.scala_tools.time.Imports._
import bootstrap.liftweb.SnogginInjector
import rhyskeepence.Clock
import rhyskeepence.storage.MongoDataPointStore

class Loader(mongoStore: MongoDataPointStore, dataPointSource: FileToDataPointAdaptor, clock: Clock, subscriber: LoaderSubscriber) extends Actor with Logger {
  def receive = {
    case TriggerLoad =>
      val fromDate = mongoStore.lastModified.toDateMidnight.plusDays(1).toDateTime
      info("Loader: reading content modified since " + fromDate)

      dataPointSource.processDataPointsSince(fromDate) { dataPoints =>
          info("Loader: inserting " + dataPoints.size + " data points...")
          mongoStore write dataPoints
          info("Loader: done inserting")
      }

      subscriber.notifyLoadFinished()
  }
}

case object TriggerLoad

object Loader extends Logger {

  val initialLoadAtOneAm = Props.getBool("only.load.at.oneam", true)
  val collectionDirectory = Props.get("loader.directory", ".")

  val cache = SnogginInjector.cache.vend
  val clock = SnogginInjector.clock.vend
  val mongoStore = SnogginInjector.mongoStore.vend
  val dataPointSource = new FileToDataPointAdaptor(
    new StatisticFileSource(collectionDirectory, clock),
    new CsvStatisticsFileParser
  )
  val subscriber = new InvalidateCacheAfterLoad(cache)

  def start() = {
    val durationTillStart = calculateDurationToFirstLoad
    info("Scheduling next load at " + (clock.now + durationTillStart))

    val contentLoadingActor = actorOf(new Loader(mongoStore, dataPointSource, clock, subscriber)).start()

    schedule(
      contentLoadingActor,
      TriggerLoad,
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