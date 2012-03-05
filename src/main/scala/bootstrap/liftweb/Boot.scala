package bootstrap.liftweb

import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
import net.liftweb.util.Helpers
import rhyskeepence.datacollection.udp.UdpListener

class Boot {
  def boot() {

    LiftRules.addToPackages("rhyskeepence")

    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(makeUtf8)
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    LiftRules.defaultHeaders = {
      case _ =>
        List(
          "Pragma" -> "private",
          "Cache-Control" -> "max-age=3600",
          "Expires" -> Helpers.toInternetDate(Helpers.now.getTime + 3600000)
        )
    }

    UdpListener.start()

    LiftRules.unloadHooks.append {
      UdpListener.shutdown _
    }

  }

  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }
}
