package observatory

import java.io.File

import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.JpegWriter

/**
  * Launch with setsid nohup sbt "runMain observatory.ImagePreparator" 2>&1 > outputLog.txt &
  */

object ImagePreparator {
  def main(args: Array[String]): Unit = {
    // Compute the tiles for all the years between 1975 and 2015.
    // Put the generated images in the following location: “target/temperatures/<year>/<zoom>/<x>-<y>.png”.
    // Compute normals from yearly temperatures between 1975 and 1989 ;
    // Compute deviations for years between 1990 and 2015 ;
    // Generate tiles for zoom levels going from 0 to 3, showing the deviations.
    // Write the tiles on your file system, under a location named according to the following scheme:
    // “target/deviations/<year>/<zoom>/<x>-<y>.png”.
    
    val stationsPath = "/stations.csv"
    var averageFeedTemperatures = List.empty[Iterable[(Location, Double)]]
    var averageFunc: (Int, Int) => Double = Manipulation.average(List.empty[Iterable[(Location, Double)]])

    (1975 to 2015).foreach { year =>
      val temperaturesFilePath = s"/$year.csv"

      println("Ectrating year " + year)
      val yearTemperatures = Extraction.locationYearlyAverageRecords(
        Extraction.locateTemperatures(year, stationsPath, temperaturesFilePath)
      )

      println("Processing temperatures image " + year)
      Interaction.generateTiles(
        Seq((year, yearTemperatures)),
        getSaveImgFunc("temperatures", temperaturesColors, Interaction.tile)
      )

      if (year == 1990) {
        println("Procesiing average")
        averageFunc = Manipulation.average(averageFeedTemperatures)
      }

      if ((1975 to 1989).contains(year)) {
        averageFeedTemperatures = yearTemperatures :: averageFeedTemperatures
      } else {
        val deviationFunc = Manipulation.deviation(yearTemperatures, averageFunc)

        println("Processing deviation image " + year)
        Interaction.generateTiles(
          Seq((year, deviationFunc)),
          getSaveImgFunc("deviations", deviationsColors, Visualization2.visualizeGrid)
        )
      }
    }
  }
  
  private def getSaveImgFunc[Data](
                                    folderName: String,
                                    colors: Seq[(Double, Color)],
                                    tileFunc: (Data, Seq[(Double, Color)], Int, Int, Int) => Image
                                  ): (Int, Int, Int, Int, Data) => Unit = {
  
    implicit val writer = JpegWriter().withCompression(50).withProgressive(true)
    
    (year: Int, zoom: Int, x: Int, y: Int, data: Data) => {
      val folderPath = s"target/$folderName/$year/$zoom"
      val img = tileFunc(data, colors, zoom, x, y)
      createIfNotExist(folderPath)
      img.output(new java.io.File(s"$folderPath/$x-$y.png"))
    }
    
  }
  
  def createIfNotExist(path: String): Unit = {
    val folder = new File(path)
    if (!folder.exists()) {
      val newPath = path.split("/").dropRight(1).mkString("/")
      createIfNotExist(newPath)
      folder.mkdir()
    }
  }
  
  val temperaturesColors = Seq(
    (60D,	Color(255,	255,	255)),
    (32D,	Color(255,	0,	0)),
    (0D,	Color(0,	255,	255)),
    (12D,	Color(255,	255,	0)),
    (-15D,	Color(0,	0,	255)),
    (-27D,	Color(255,	0,	255)),
    (-50D,	Color(33,	0,	107)),
    (-60D,	Color(0,	0,	0))
  )
  
  val deviationsColors = Seq(
    (7D, Color(0, 0, 0)),
    (4D, Color(255, 0, 0)),
    (2D, Color(255, 255, 0)),
    (0D, Color(255, 255, 255)),
    (-2D, Color(0, 255, 255)),
    (-7D, Color(0, 0, 255))
  )
}
