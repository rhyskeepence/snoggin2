package snoggin.restful

import org.specs2.clairvoyance.ClairvoyantSpec
import snoggin.StatsGeneration._

class CsvRendererSpec extends ClairvoyantSpec {

  "CSV renderer" should {
    "render empty stats as empty CSV" in {
      val stats: Stats = Map()
      CsvRenderer.renderAsCsv(stats) must_==("No data")
    }

    "render single stat as CSV" in {
      val stats: Stats = Map("ponies" -> Map(1.0 -> 5.0, 2.0 -> 10.0))
      CsvRenderer.renderAsCsv(stats) must_==("timestamp,ponies\n1.0,5.0\n2.0,10.0")
    }

    "render multiple stat as CSV" in {
      val stats: Stats = Map("ponies" -> Map(1.0 -> 5.0, 2.0 -> 10.0), "puppies" -> Map(1.0 -> 15.0, 2.0 -> 20.0, 3.0 -> 30.0))
      CsvRenderer.renderAsCsv(stats) must_==("timestamp,ponies,puppies\n1.0,5.0,15.0\n2.0,10.0,20.0\n3.0,0.0,30.0")
    }
  }
}
