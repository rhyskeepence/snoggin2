package rhyskeepence.legacyadaptor

import org.specs.mock.Mockito
import org.specs.SpecificationWithJUnit
import rhyskeepence.storage.MongoDataPointStore
import rhyskeepence.Clock
import org.scala_tools.time.Imports._
import rhyskeepence.model.DataPoint
import akka.testkit.TestActorRef

class LoaderTest extends SpecificationWithJUnit with Mockito {
  val lastModifiedDate = new DateTime(2012, 01, 01, 23, 50, 0, 0)
  val expectedNextDate = new DateTime(2012, 01, 02, 0, 0, 0, 0)

  val dataPoint = DataPoint(expectedNextDate.getMillis, "env", List())
  
  val mongoStore = mock[MongoDataPointStore]
  val fileToDataPointAdaptor = new StubFileToDataPointAdaptor(expectedNextDate, dataPoint)
  val clock = mock[Clock]
  val subscriber = mock[LoaderSubscriber]

  val loaderActorRef = TestActorRef(new Loader(mongoStore, fileToDataPointAdaptor, clock, subscriber))
  val loader = loaderActorRef.underlyingActor

  "load next day's CSV file into MongoDB" in {
    mongoStore.lastModified returns lastModifiedDate
    
    loader receive TriggerLoad

    there was one(mongoStore).write(List(dataPoint))
  }
  
  class StubFileToDataPointAdaptor(expectedDate: DateTime, singleDataPoint: DataPoint) extends FileToDataPointAdaptor(null, null) {
    override def processDataPointsSince[T](startingDate: DateTime)(process: (List[DataPoint]) => T) {
      startingDate must_== expectedDate
      process(List(singleDataPoint))
    }
  }
}