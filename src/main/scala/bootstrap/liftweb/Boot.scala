package bootstrap.liftweb

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.provider._
import net.liftweb.util.Helpers._
import net.liftweb.util.Helpers
import snoggin.restful.SnogginRestService

class Boot {
  def boot() {

    LiftRules.addToPackages("snoggin")

    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.unusedFunctionsLifeTime = 1.hour

    LiftRules.early.append(makeUtf8)
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))
    LiftRules.statelessDispatchTable.append(SnogginRestService)

    LiftRules.defaultHeaders = {
      case _ =>
        List(
          "Pragma" -> "private",
          "Cache-Control" -> "max-age=3600",
          "Expires" -> Helpers.toInternetDate(Helpers.now.getTime + 3600000)
        )
    }
  }

  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }
}
