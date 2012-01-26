package rhyskeepence.legacyadaptor

import org.specs.Specification
import org.specs.mock.Mockito
import io.Source
import org.joda.time.DateTime
import rhyskeepence.model.{Metric, DataPoint}

class CsvStatisticsFileParserTest extends Specification with Mockito {

  val parser = new CsvStatisticsFileParser
  val source = mock[Source]
  val statisticFile = mock[StatisticFile]

  val rowOneTime = new DateTime("2012-01-15T01:00:00").getMillis
  val rowTwoTime = new DateTime("2012-01-15T02:00:00").getMillis

  "parse csv file" in {
    
    source.getLines() returns Iterator(
      "time,metric1,metric2",
      rowOneTime + ",222,333",
      rowTwoTime + ",444,555"
    )

    statisticFile.source returns source
    statisticFile.environment returns "knitware"

    val points = parser.getDataPointsFrom(statisticFile)

    points mustContain DataPoint(rowOneTime, "knitware", List(Metric("metric1", 222), Metric("metric2", 333)))
    points mustContain DataPoint(rowTwoTime, "knitware", List(Metric("metric1", 444), Metric("metric2", 555)))

  }
}