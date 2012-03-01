package rhyskeepence.datacollection.udp

import net.liftweb.util.Props
import net.liftweb.common.Logger
import java.util.concurrent.atomic.AtomicBoolean
import java.io.IOException
import akka.actor._
import akka.actor.Actor._
import java.net._

object UdpListener {
  val listener = actorOf[UdpListener].start()

  def start() {
    listener ! Start
  }

  def shutdown() {
    listener ! Stop
  }
}

case object Start
case object Stop

class UdpListener extends Actor with Logger {

  val port = Props.getInt("udp.listen.port", 8011)
  var shouldListen = new AtomicBoolean(true)

  def receive = {
    case Start =>
      val listener = new DatagramSocket(port)
      listener.setSoTimeout(1000)

      val connectionHandler = actorOf[UdpConnectionHandler].start()

      info("Listening for UDP connections on port %d".format(port))

      while (shouldListen.get) {
        try {
          val buffer = Array.ofDim[Byte](1024)
          val receivedPacket = new DatagramPacket(buffer, buffer.length)

          listener.receive(receivedPacket)
          connectionHandler ! IncomingConnection(receivedPacket)

        } catch {
          case timeout: SocketTimeoutException => // expected, will just loop
          case ioe: IOException => error("problem accpeting connection", ioe)
        }
      }

      listener.close()

    case Stop =>
      shouldListen set false
  }
}

