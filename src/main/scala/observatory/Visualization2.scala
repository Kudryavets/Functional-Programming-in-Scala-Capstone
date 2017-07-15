package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Interaction.tileLocation
import observatory.Visualization.interpolateColor

import scala.math._

/**
  * 5th milestone: value-added information visualization
  */
object Visualization2 {

  /**
    * @param x X coordinate between 0 and 1
    * @param y Y coordinate between 0 and 1
    * @param d00 Top-left value
    * @param d01 Bottom-left value
    * @param d10 Top-right value
    * @param d11 Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(
    x: Double,
    y: Double,
    d00: Double,
    d01: Double,
    d10: Double,
    d11: Double
  ): Double = {
    val xCeilInterpolated = (1-x) * d00 + x * d10
    val xFloorInterpolated = (1-x) * d01 + x * d11
    (1-y) * xCeilInterpolated + y * xFloorInterpolated
  }

  /**
    * @param grid Grid to visualize
    * @param colors Color scale to use
    * @param zoom Zoom level of the tile to visualize
    * @param x X value of the tile to visualize
    * @param y Y value of the tile to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(
    grid: (Int, Int) => Double,
    colors: Iterable[(Double, Color)],
    zoom: Int,
    x: Int,
    y: Int
  ): Image = {
    val imageSize = 128
    val pixelArray: Array[Pixel] = new Array[Pixel](imageSize*imageSize)
    val colorsList = colors.toList

    ( for { xCoord <- 0 until imageSize; yCoord <- 0 until imageSize } yield (xCoord, yCoord) ).toVector.par
      .foreach { case (xCoord, yCoord) =>
        val location = tileLocation(zoom + 8, x * imageSize + xCoord*2, y * imageSize + yCoord*2)
        val xFloor = floor(location.lon).toInt
        val xCeil = ceil(location.lon).toInt
        val yFloor = floor(location.lat).toInt
        val yCeil = ceil(location.lat).toInt
        val temp = bilinearInterpolation(
          location.lon - xFloor,
          yCeil - location.lat,
          grid(yCeil, xFloor),
          grid(yFloor, xFloor),
          grid(yCeil, xCeil),
          grid(yFloor, xCeil)
        )
        val color = interpolateColor(colorsList, temp)
        val pixel = Pixel(color.red, color.green, color.blue, 127)
        pixelArray(yCoord * imageSize + xCoord) = pixel
      }

    Image(imageSize, imageSize, pixelArray).fit(2*imageSize,2*imageSize)
  }

}
