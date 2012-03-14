package rhyskeepence.caching

import org.specs.Specification
import rhyskeepence.queries.Aggregator
import org.joda.time.Interval
import org.scala_tools.time.Imports._

import scala.collection.mutable.ListBuffer
import com.mongodb.DBObject
import bootstrap.liftweb.SnogginInjector
import org.specs.mock.Mockito

class CacheableSpec extends Specification with Mockito {

  val cache = mock[SnogginCache]
  SnogginInjector.cache.default.set(() => cache)

  "The cacheable trait" should {
    val cacheable = new StubAggregator with Cacheable

    "multiplex aggregations by day" in {
      cache.get(any) returns None

      val tenDayInterval = DateTime.now - 10.days to DateTime.now
      cacheable.aggregate("x", "y", tenDayInterval)

      cacheable.intervals.size must_== 11
    }

    "cache aggregations" in {
      cache.get(any) returns None
      cacheable.aggregate("x", "y", DateTime.lastHour to DateTime.now)
      there was one(cache).put(any, any)

    }
  }

  class StubAggregator extends Aggregator {
    val intervals = new ListBuffer[Interval]
    
    def aggregate(environment: String, metricName: String, duration: Interval): List[DBObject] = {
      intervals += duration
      List()
    }
  }
}
