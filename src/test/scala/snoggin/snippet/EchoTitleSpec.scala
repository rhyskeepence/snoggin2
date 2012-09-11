package snoggin.snippet

import net.liftweb.mockweb.WebSpec
import net.liftweb.mocks.MockHttpServletRequest

class EchoTitleSpec extends WebSpec {

  "The Echo Title snippet" should {
    val echoTitleSnippet = new EchoTitle()

    "echo the title parameter to the page" withSFor(requestWithTitle("How+now+brown+cow")) in {
      val xml = echoTitleSnippet.render(<h1>title</h1>)
      xml must \\(<h1>How now brown cow</h1>)
    }

    "default to empty when no title parameter" withSFor(requestWithNoTitle) in {
      val xml = echoTitleSnippet.render(<h1>title</h1>)
      xml must \\(<h1></h1>)
    }

  }

  def requestWithTitle(title: String) = {
    new MockHttpServletRequest("/?title=" + title)
  }

  def requestWithNoTitle = {
    new MockHttpServletRequest("/")
  }
}
