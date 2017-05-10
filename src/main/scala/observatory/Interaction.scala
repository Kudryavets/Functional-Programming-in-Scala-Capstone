package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Visualization.{interpolateColor, predictTemperature}

import scala.math._

/**
  * 3rd milestone: interactive visualization
  */
object Interaction {

  /**
    * @param zoom Zoom level
    * @param x X coordinate
    * @param y Y coordinate
    * @return The latitude and longitude of the top-left corner of the tile, as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    */
  def tileLocation(zoom: Int, x: Int, y: Int): Location =
    Location(
      atan(sinh(Pi - 2*Pi*y/pow(2, zoom)))*180/Pi,
      360*x/pow(2, zoom) - 180
    )

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @param zoom Zoom level
    * @param x X coordinate
    * @param y Y coordinate
    * @return A 256×256 image showing the contents of the tile defined by `x`, `y` and `zooms`
    */
  def tile(temperatures: Iterable[(Location, Double)], colors: Iterable[(Double, Color)], zoom: Int, x: Int, y: Int): Image = {
    val imageSize = 256
    val pixelArray: Array[Pixel] = new Array[Pixel](imageSize*imageSize)
    val colorsList = colors.toList
    val temperaturesVec = temperatures.toVector

    ( for { xCoord <- 0 until imageSize; yCoord <- 0 until imageSize } yield (xCoord, yCoord) ).toVector.par
      .foreach { case (xCoord, yCoord) =>
        val temp = predictTemperature(temperaturesVec, tileLocation(zoom + 8, x * imageSize + xCoord, y * imageSize + yCoord))
        val color = interpolateColor(colorsList, temp)
        val pixel = Pixel(color.red, color.green, color.blue, 127)
        pixelArray(yCoord * imageSize + xCoord) = pixel
      }

    Image(imageSize, imageSize, pixelArray)
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    * @param yearlyData Sequence of (year, data), where `data` is some data associated with
    *                   `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data](
    yearlyData: Iterable[(Int, Data)],
    generateImage: (Int, Int, Int, Int, Data) => Unit
  ): Unit = {
    yearlyData.foreach { case (year, data) =>
      for {
        zoom <- 0 to 3
        x <- 0 until pow(2, zoom).toInt
        y <- 0 until pow(2, zoom).toInt
      } generateImage(year, zoom, x, y, data)
    }
  }
}
