package rhyskeepence.snippet

import rhyskeepence.WebSpecSetupAndTearDown
import net.liftweb.mockweb.WebSpec
import net.liftweb.mocks.MockHttpServletRequest

class StatsTest extends WebSpec with WebSpecSetupAndTearDown {

  "The stats snippet" should {

    val r = new MockHttpServletRequest("http://noggin/chart.html?fields=production-activemq:ActiveMqknitwareToHaloQueueSize")

    "parse fields" withSFor(r) in {
//      S.runTemplate("")
    }

  }

}