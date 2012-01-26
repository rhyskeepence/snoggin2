package rhyskeepence.legacyadaptor

import org.specs.Specification
import org.specs.mock.Mockito
import java.io.File
import org.scala_tools.time.Imports._
import rhyskeepence.Clock

class StatisticFileSourceTest extends Specification with Mockito {

  val collectionDirectory = System.getProperty("java.io.tmpdir") + "/stats-file-source"
  val clock = mock[Clock]
  val fileSource = new StatisticFileSource(collectionDirectory, clock)

  "The StatisticFileSource" should {

    doBefore {
      new File(collectionDirectory).mkdirs
    }

    doAfter {
      deleteRecursive(new File(collectionDirectory))
    }

    "given a duration, find files which were created within the specific time period" in {
      clock.midnight returns new DateTime(2012, 1, 6, 0, 0, 0, 0)
      createStatsFile("20111231/production-knitware-diamondquartz-20111231.csv")
      createStatsFile("20120101/production-knitware-diamondquartz-20120101.csv")
      createStatsFile("20120102/production-knitware-diamondquartz-20120102.csv")
      createStatsFile("20120103/production-knitware-diamondquartz-20120103.csv")
      createStatsFile("20120104/production-knitware-diamondquartz-20120104.csv")
      createStatsFile("20120105/production-knitware-diamondquartz-20120105.csv")

      val matchingFiles = fileSource.filesFor(5.days)

      matchingFiles.size must_== 5
      matchingFiles(0).environment must_== "production-knitware"
    }

    "do not include today, as today's file will be incomplete" in {
      clock.midnight returns new DateTime(2012, 1, 6, 0, 0, 0, 0)
      createStatsFile("20120105/production-knitware-diamondquartz-20120105.csv")
      createStatsFile("20120106/production-knitware-diamondquartz-20120106.csv")

      val matchingFiles = fileSource.filesFor(5.days)

      matchingFiles.size must_== 1
    }
  }

  "the StatisticFile must extract the environment name from the file name" in {
    var file = StatisticFile(new File("production-knitware-diamondquartz-20120106.csv"))
    file.environment must_== "production-knitware"
  }

  private def createStatsFile(filename: String) {
    val file = new File(collectionDirectory, filename)
    file.getParentFile.mkdirs
    file.createNewFile
  }

  private def deleteRecursive(baseFile : File) {
    if(baseFile.isDirectory)
      baseFile.listFiles.foreach{ f => deleteRecursive(f) }
    baseFile.delete
  }
}