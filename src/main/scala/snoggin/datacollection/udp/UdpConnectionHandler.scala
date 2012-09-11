package snoggin.datacollection.udp

import java.net.DatagramPacket
import snoggin.model.{Metric, DataPoint}
import scala.io.Source
import bootstrap.liftweb.SnogginInjector
import net.liftweb.common.Logger
import akka.actor.Actor

class UdpConnectionHandler extends Actor with Logger {
  val inputPattern = """([\w\-]+):([\w\-]+)=(\d+)""".r
  val dataPointStore = SnogginInjector.mongoStore.vend

  def receive = {
    case IncomingConnection(packet) =>
      val input = Source.fromBytes(packet.getData).getLines().next().trim
      info("Incoming connection from " + packet.getAddress + " with data " + input)

      input match {
        case inputPattern(environment, metricName, value) =>
          dataPointStore.write(
            DataPoint(System.currentTimeMillis(), environment,
              List(
                Metric(metricName, value.toDouble)
              )
            )
          )

        case _ =>
          error("Received invalid input: [%s] - must be in the format " +
            "'[environment]:[metric-name]=[numeric-value]'".format(input))
      }

  }
}

case class IncomingConnection(packet: DatagramPacket)