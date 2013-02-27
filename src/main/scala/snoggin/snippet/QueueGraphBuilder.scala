package snoggin.snippet

import net.liftweb.util.Helpers._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.SHtml._
import net.liftweb.common.Full
import net.liftweb.http.SHtml
import snoggin.queries.mongo.FindApplicationMetric
import collection.immutable.{ListMap, TreeMap}

class QueueGraphBuilder {
  val findApplicationMetric = new FindApplicationMetric

  var brokerName = ""
  var queueName = ""
  var queueFunction = ""

  def render = {
    val onBrokerChange = ajaxCall(JE.JsRaw("this.value"), s => replaceQueuesJs(s))._2.toJsCmd

    val brokerNames = findApplicationMetric.applicationNames.filter(_.startsWith("activemq"))
    brokerName = brokerNames.head

    "#brokers" #> select(brokerNames.map(app => (app, app)), Full(brokerName), brokerName = _, "onchange" -> onBrokerChange) &
    "#queues" #> untrustedSelect(queuesFor(brokerName), Full(queueName), queueName = _) &
    "#function" #> untrustedSelect(queueFunctions.keys.toSeq.map(function => (function,function)), Full(queueFunction), queueFunction = _) &
    "#render_button" #> SHtml.ajaxSubmit("Render", () => RedirectTo(queueFunctions(queueFunction)())) andThen SHtml.makeFormsAjax
  }

  private def replaceQueuesJs(application: String): JsCmd = {
    val queues = queuesFor(application)
    val first = queues.head._1
    ReplaceOptions("queues", queues, Full(first))
  }

  def queuesFor(application: String): List[(String,String)] = {
    findApplicationMetric.metricNamesFor(application)
      .filter(_.endsWith("QueueSize"))
      .map(_.replace("ActiveMq", "").replace("QueueSize", ""))
      .map(s => (s,formatQueueName(s)))
  }

  def formatQueueName(s: String): String = {
    s.replaceAll("([A-Z][a-z])", " $1").capitalize
  }

  val queueFunctions = ListMap(
    "size" -> size,
    "daily enqueue/dequeue count" -> dailyEnqueueDequeueCount,
    "traversal time in seconds" -> traversalTimeInSeconds,
    "enqueue throughput per second" -> enqueueThroughput,
    "dequeue throughput per second" -> dequeueThroughput
  )

  def size = () => "chart.html?fields=%s:ActiveMq%sQueueSize&days=7&title=%s - queue size over the past 7 days".format(brokerName, queueName, formatQueueName(queueName))
  def dailyEnqueueDequeueCount = () => "chart.html?fields=%s:ActiveMq%sEnqueueCountThroughputPerSecond,%s:ActiveMq%sDequeueCountThroughputPerSecond&days=7&aggregate=dailythroughput&title=%s - daily enqueue/dequeue count over the past 7 days".format(brokerName, queueName, brokerName, queueName, formatQueueName(queueName))
  def traversalTimeInSeconds = () => "chart.html?fields=%s:ActiveMq%sQueueSize,%s:ActiveMq%sDequeueCountThroughputPerSecond&days=7&semigroup=divide&title=%s - queue traversal time in seconds over the past 7 days".format(brokerName, queueName, brokerName, queueName, formatQueueName(queueName))
  def enqueueThroughput = () => "chart.html?fields=%s:ActiveMq%sEnqueueCountThroughputPerSecond&days=0&title=%s - enqueue throughput per second (today)".format(brokerName, queueName, formatQueueName(queueName))
  def dequeueThroughput = () => "chart.html?fields=%s:ActiveMq%sDequeueCountThroughputPerSecond&days=0&title=%s - dequeue throughput per second (today)".format(brokerName, queueName, formatQueueName(queueName))
}