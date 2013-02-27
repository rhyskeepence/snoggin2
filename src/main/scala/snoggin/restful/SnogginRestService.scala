package snoggin.restful

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.PlainTextResponse
import snoggin.{StatsGeneration, StatsRequestParser}

object SnogginRestService extends RestHelper {
  val requestParser = new StatsRequestParser()

  serve {
    // respond to GET /api/csv
    case "api" :: "csv" :: Nil Get _ => renderCsvStats
  }

  def renderCsvStats = {
    val statsRequest = requestParser.buildStatsRequest
    val stats = StatsGeneration.generateStats(statsRequest)
    PlainTextResponse(CsvRenderer.renderAsCsv(stats))
  }
}