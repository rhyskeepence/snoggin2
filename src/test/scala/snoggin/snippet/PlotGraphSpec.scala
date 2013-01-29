package snoggin.snippet

import net.liftweb.mockweb.WebSpec
import net.liftweb.mocks.MockHttpServletRequest
import bootstrap.liftweb.SnogginInjector
import org.specs.mock.Mockito
import snoggin.queries.{Aggregator, AggregatorFactory}
import snoggin.Clock
import org.joda.time.{Period, Interval, DateTime}

class PlotGraphSpec extends WebSpec with Mockito {

  val aggregatorFactory = mock[AggregatorFactory]
  SnogginInjector.aggregatorFactory.default.set(() => aggregatorFactory)

  val now = DateTime.now
  val clock = mock[Clock]
  clock.now returns now
  SnogginInjector.clock.default.set(() => clock)

  val aggregator = mock[Aggregator]
  aggregator.aggregate(any, any, any) returns List()

  val plotGraphSnippet = new PlotGraph()

  "The Plot Graph snippet" should {

    "tokenize fields and call aggregator" withSFor (requestFor("/chart.html?fields=halo:oranges,knitware:bananas")) in {
      aggregatorFactory.noAggregationMultipleDays returns aggregator
      plotGraphSnippet.render
      there was one(aggregator).aggregate(be_==("halo"), be_==("oranges"), any)
      there was one(aggregator).aggregate(be_==("knitware"), be_==("bananas"), any)
    }

    "use the default aggregator when no aggregate parameter is specified" withSFor (requestFor("/chart.html?fields=x:y")) in {
      aggregatorFactory.noAggregationMultipleDays returns aggregator
      plotGraphSnippet.render
      there was one(aggregator).aggregate(any, any, any)
    }

    "use the sum per day aggregator when requested" withSFor (requestFor("/chart.html?fields=x:y&aggregate=sum")) in {
      aggregatorFactory.sumPerDay returns aggregator
      plotGraphSnippet.render
      there was one(aggregator).aggregate(any, any, any)
    }

    "use the average per day aggregator when requested" withSFor (requestFor("/chart.html?fields=x:y&aggregate=average")) in {
      aggregatorFactory.averagePerDay returns aggregator
      plotGraphSnippet.render
      there was one(aggregator).aggregate(any, any, any)
    }

    "use the daily throughput aggregator when requested" withSFor (requestFor("/chart.html?fields=x:y&aggregate=dailythroughput")) in {
      aggregatorFactory.dailyThroughput returns aggregator
      plotGraphSnippet.render
      there was one(aggregator).aggregate(any, any, any)
    }

    "use the errors per day aggregator when requested" withSFor (requestFor("/chart.html?fields=x:y&aggregate=errors")) in {
      aggregatorFactory.errorsPerDay returns aggregator
      plotGraphSnippet.render
      there was one(aggregator).aggregate(any, any, any)
    }

    "use a default of 7 days as the aggregation time" withSFor (requestFor("/chart.html?fields=x:y")) in {
      aggregatorFactory.noAggregationMultipleDays returns aggregator
      plotGraphSnippet.render
      there was one(aggregator).aggregate(any, any, be_==(new Interval(Period.days(7), now)))
    }

    "use the number of days specified as the aggregation time" withSFor (requestFor("/chart.html?fields=x:y&days=10")) in {
      aggregatorFactory.noAggregationMultipleDays returns aggregator
      plotGraphSnippet.render
      there was one(aggregator).aggregate(any, any, be_==(new Interval(Period.days(10), now)))
    }

    "use the single day aggregator" withSFor (requestFor("/chart.html?fields=x:y&days=1")) in {
      aggregatorFactory.noAggregationOneDay returns aggregator
      plotGraphSnippet.render
      there was one(aggregator).aggregate(any, any, be_==(new Interval(Period.days(1), now)))
    }

  }

  def requestFor(s: String) = new MockHttpServletRequest(s)

}
