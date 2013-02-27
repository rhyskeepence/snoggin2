package snoggin.snippet

import net.liftweb.util.Props

class UdpPort {
  def render() = {
    val port = Props.getInt("udp.listen.port", 8011)
    <span>{port}</span>
  }
}
