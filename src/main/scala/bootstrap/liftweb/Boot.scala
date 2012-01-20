package bootstrap.liftweb

import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._

class Boot {
  def boot() {

    LiftRules.addToPackages("rhyskeepence")

    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(makeUtf8)
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

  }

  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }
}
