package snoggin

import net.liftweb.mockweb.WebSpec
import net.liftweb.mocks.MockHttpServletRequest
import bootstrap.liftweb.SnogginInjector
import org.specs.mock.Mockito
import snoggin.queries.{Aggregator, AggregatorFactory}
import org.joda.time.{Period, Interval, DateTime}

class StatsRequestParserSpec extends WebSpec with Mockito {

  val aggregatorFactory = mock[AggregatorFactory]
  SnogginInjector.aggregatorFactory.default.set(() => aggregatorFactory)

  val now = DateTime.now
  val clock = mock[Clock]
  clock.now returns now
  SnogginInjector.clock.default.set(() => clock)

  val aggregator = mock[Aggregator]
  aggregator.aggregate(any, any, any) returns List()

  "The Plot Graph snippet" should {

    "tokenize fields and call aggregator" withSFor (requestFor("/chart.html?fields=halo:oranges,knitware:bananas")) in {
      aggregatorFactory.noAggregationMultipleDays returns aggregator
      val request = new StatsRequestParser().buildStatsRequest
      request.fields mustEqual List("halo:oranges", "knitware:bananas")
    }

    "use the default aggregator when no aggregate parameter is specified" withSFor (requestFor("/chart.html?fields=x:y")) in {
      aggregatorFactory.noAggregationMultipleDays returns aggregator
      val request = new StatsRequestParser().buildStatsRequest
      request.aggregator mustBe aggregator
    }

    "use the sum per day aggregator when requested" withSFor (requestFor("/chart.html?fields=x:y&aggregate=sum")) in {
      aggregatorFactory.sumPerDay returns aggregator
      val request = new StatsRequestParser().buildStatsRequest
      request.aggregator mustBe aggregator
    }

    "use the average per day aggregator when requested" withSFor (requestFor("/chart.html?fields=x:y&aggregate=average")) in {
      aggregatorFactory.averagePerDay returns aggregator
      val request = new StatsRequestParser().buildStatsRequest
      request.aggregator mustBe aggregator
    }

    "use the daily throughput aggregator when requested" withSFor (requestFor("/chart.html?fields=x:y&aggregate=dailythroughput")) in {
      aggregatorFactory.dailyThroughput returns aggregator
      val request = new StatsRequestParser().buildStatsRequest
      request.aggregator mustBe aggregator
    }

    "use the errors per day aggregator when requested" withSFor (requestFor("/chart.html?fields=x:y&aggregate=errors")) in {
      aggregatorFactory.errorsPerDay returns aggregator
      val request = new StatsRequestParser().buildStatsRequest
      request.aggregator mustBe aggregator
    }

    "use a default of 7 days as the aggregation time" withSFor (requestFor("/chart.html?fields=x:y")) in {
      aggregatorFactory.noAggregationMultipleDays returns aggregator
      val request = new StatsRequestParser().buildStatsRequest
      request.chartPeriod mustEqual new Interval(Period.days(7), now)
    }

    "use the number of days specified as the aggregation time" withSFor (requestFor("/chart.html?fields=x:y&days=10")) in {
      aggregatorFactory.noAggregationMultipleDays returns aggregator
      val request = new StatsRequestParser().buildStatsRequest
      request.chartPeriod mustEqual new Interval(Period.days(10), now)
    }

    "use the single day aggregator" withSFor (requestFor("/chart.html?fields=x:y&days=1")) in {
      aggregatorFactory.noAggregationOneDay returns aggregator
      val request = new StatsRequestParser().buildStatsRequest
      request.aggregator mustBe aggregator
      request.chartPeriod mustEqual new Interval(Period.days(1), now)
    }
  }

  def requestFor(s: String) = new MockHttpServletRequest(s)

}
