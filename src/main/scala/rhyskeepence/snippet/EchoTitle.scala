package rhyskeepence.snippet

import net.liftweb.util.Helpers._
import net.liftweb.http.S

class EchoTitle {
  def render = {
    "h1" #> <h1>{ S.param("title").openOr("") }</h1>
  }
}
