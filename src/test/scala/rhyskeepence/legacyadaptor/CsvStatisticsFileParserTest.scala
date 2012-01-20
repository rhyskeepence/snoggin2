package rhyskeepence.legacyadaptor

import org.specs.Specification
import org.specs.mock.Mockito
import io.Source
import org.joda.time.DateTime
import rhyskeepence.model.DataPoint

class CsvStatisticsFileParserTest extends Specification with Mockito {

  val parser = new CsvStatisticsFileParser("knitware")
  val source = mock[Source]

  val rowOneTime = new DateTime("2012-01-15T01:00:00").getMillis
  val rowTwoTime = new DateTime("2012-01-15T02:00:00").getMillis


  "parse csv file" in {
    
    source.getLines() returns Iterator(
      "time,metric1,metric2",
      rowOneTime + ",222,333",
      rowTwoTime + ",444,555"
    )

    val points = parser.getDataPointsFrom(source)

    points mustContain DataPoint(rowOneTime, "knitware-metric1", 222)
    points mustContain DataPoint(rowTwoTime, "knitware-metric1", 444)

    points mustContain DataPoint(rowOneTime, "knitware-metric2", 333)
    points mustContain DataPoint(rowTwoTime, "knitware-metric2", 555)

  }
}