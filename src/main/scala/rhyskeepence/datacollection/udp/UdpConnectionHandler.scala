package rhyskeepence.datacollection.udp

import java.net.DatagramPacket
import rhyskeepence.model.{Metric, DataPoint}
import scala.io.Source
import bootstrap.liftweb.SnogginInjector
import net.liftweb.common.Logger
import akka.actor.Actor

class UdpConnectionHandler extends Actor with Logger {
  val inputPattern = """(\w+):(\w+)=(\d+)""".r
  val dataPointStore = SnogginInjector.mongoStore.vend

  def receive = {
    case IncomingConnection(packet) =>
      val input = Source.fromBytes(packet.getData).getLines().next().trim
      info("Incoming connection from " + packet.getAddress + " with data " + input)

      input match {
        case inputPattern(environment, metricName, value) =>
          val metric = Metric(metricName, value.toDouble)
          val dataPoint = DataPoint(System.currentTimeMillis(), environment, List(metric))
          dataPointStore.write(dataPoint)

        case _ =>
          error("Received invalid input: [%s] - must be in the format " +
            "'[environment]:[metric-name]=[numeric-value]'".format(input))
      }

  }
}

case class IncomingConnection(packet: DatagramPacket)
