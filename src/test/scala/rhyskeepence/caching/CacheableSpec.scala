package rhyskeepence.caching

import org.specs.Specification
import rhyskeepence.queries.Aggregator

import scala.collection.mutable.ListBuffer
import com.mongodb.DBObject
import bootstrap.liftweb.SnogginInjector
import org.specs.mock.Mockito
import org.joda.time.{Duration, DateTime, Interval}

class CacheableSpec extends Specification with Mockito {

  val cache = mock[SnogginCache]
  SnogginInjector.cache.default.set(() => cache)

  "The cacheable trait" should {
    val cacheable = new StubAggregator with Cacheable

    "multiplex aggregations by day" in {
      cache.get(any) returns None

      val tenDayInterval = new Interval(DateTime.now.minus(Duration.standardDays(10)), DateTime.now)
      cacheable.aggregate("x", "y", tenDayInterval)

      cacheable.intervals.size must_== 11
    }

    "cache aggregations" in {
      cache.get(any) returns None
      cacheable.aggregate("x", "y", new Interval(DateTime.now.minusHours(1), DateTime.now))
      there was one(cache).put(any, any)
    }

    val preDaylightSaving = new DateTime(2012, 3, 25, 0, 0)
    val postDaylightSaving = new DateTime(2012, 3, 26, 0, 0)

    "start time of each interval must be divisible by 86400000 so that DST does not screw up charts" in {
      cache.get(any) returns None
      val duration = new Interval(preDaylightSaving, postDaylightSaving)
      cacheable.aggregate("x", "y", duration)
      cacheable.intervals.head.getStartMillis % 86400000 must_==(0)
    }

  }

  class StubAggregator extends Aggregator {
    val intervals = new ListBuffer[Interval]
    
    def aggregate(environment: String, metricName: String, duration: Interval): List[DBObject] = {
      intervals += duration
      List()
    }

    def aggregatorName = "stub-aggregator"
  }
}
