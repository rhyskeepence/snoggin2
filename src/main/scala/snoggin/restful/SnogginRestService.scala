package snoggin.restful

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.PlainTextResponse
import snoggin.{StatsGeneration, StatsRequestParser}

object SnogginRestService extends RestHelper {

  serve {
    // respond to GET /api/csv
    case "api" :: "csv" :: Nil Get _ => renderCsvStats
  }

  def renderCsvStats = {
    val statsRequest = StatsRequestParser.buildStatsRequest
    val stats = StatsGeneration.generateStats(statsRequest)
    PlainTextResponse(CsvRenderer.renderAsCsv(stats))
  }
}