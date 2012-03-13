package rhyskeepence.snippet

import net.liftweb.util.Helpers._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.SHtml._
import net.liftweb.common.Full
import net.liftweb.http.SHtml
import rhyskeepence.queries.mongo.FindApplicationMetric

class Metrics {
  val findApplicationMetric = new FindApplicationMetric

  var applicationName = ""
  var metricName = ""

  def render = {
    val onApplicationNameChange = ajaxCall(JE.JsRaw("this.value"), s => replaceMetricsJs(s))._2.toJsCmd

    val applicationNames = findApplicationMetric.applicationNames
    applicationName = applicationNames.head

    "#applications" #> select(applicationNames.map(app => (app, app)), Full(applicationName), applicationName = _, "onchange" -> onApplicationNameChange) &
    "#metric_select" #> untrustedSelect(findApplicationMetric.metricNamesFor(applicationName).map(app => (app, app)), Full(metricName), metricName = _) &
    "#add_button" #> SHtml.ajaxSubmit("Add", () => Run("append_metric('" + applicationName + ":" + metricName + "')")) andThen SHtml.makeFormsAjax
  }

  private def replaceMetricsJs(application: String): JsCmd = {
    val metric = findApplicationMetric.metricNamesFor(application)
    val first = metric.head
    ReplaceOptions("metric_select", metric.map(s => (s,s)), Full(first))
  }
}