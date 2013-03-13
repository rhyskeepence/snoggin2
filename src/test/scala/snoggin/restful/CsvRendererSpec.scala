package snoggin.restful

import org.specs2.clairvoyance.ClairvoyantSpec
import snoggin.Stats

class CsvRendererSpec extends ClairvoyantSpec {

  "CSV renderer" should {
    "render empty stats as empty CSV" in {
      val stats = Stats(Seq())
      CsvRenderer.renderAsCsv(stats) must_==(
        "No data")
    }

    "render single stat as CSV" in {
      val stats = Stats(Seq("ponies" -> Map(1.0 -> 5.0, 2.0 -> 10.0)))
      CsvRenderer.renderAsCsv(stats) must_==(
        "timestamp,ponies\n" +
        "1970-01-01 01:00:00.001,5.0\n" +
        "1970-01-01 01:00:00.002,10.0")
    }

    "render multiple stat as CSV" in {
      val stats = Stats(Seq("ponies" -> Map(1.0 -> 5.0, 2.0 -> 10.0), "puppies" -> Map(1.0 -> 15.0, 2.0 -> 20.0, 3.0 -> 30.0)))
      CsvRenderer.renderAsCsv(stats) must_==(
        "timestamp,ponies,puppies\n" +
          "1970-01-01 01:00:00.001,5.0,15.0\n" +
          "1970-01-01 01:00:00.002,10.0,20.0\n" +
          "1970-01-01 01:00:00.003,0.0,30.0")
    }
  }
}
