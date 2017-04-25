package observatory

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers

@RunWith(classOf[JUnitRunner])
class InteractionTest extends FunSuite with Checkers {
  test("visualize grader 2") {

    val locations = List(
      (Location(45.0, -90.0), 20.0),
      (Location(45.0, 90.0), 0.0),
      (Location(0.0, 0.0), 10.0),
      (Location(-45.0, -90.0), 0.0),
      (Location(-45.0, 90.0), 20.0)
    )

    val colorMap = List(
      (0.0, Color(255, 0, 0)),
      (10.0, Color(0, 255, 0)),
      (20.0 , Color(0  , 0  , 255))
    )

    val tile_0_0_0 = Interaction.tile(locations, colorMap, 0, 0, 0)
    val pixel_0_1 = tile_0_0_0.pixels(256*20 + 20)
    val pixel_0_2 = tile_0_0_0.pixels(236*256 - 20)
    val pixel_0_3 = tile_0_0_0.pixels(226*256 + 40)
    assert(pixel_0_1.blue > pixel_0_1.red, "zoom = 0, upper left conor")
    assert(pixel_0_2.blue > pixel_0_2.red, "zoom = 0, bottom right conor")
    assert(pixel_0_3.red > pixel_0_2.blue, "zoom = 0, bottom left conor")

    val tile_1_0_0 = Interaction.tile(locations, colorMap, 1, 0, 0)
    val pixel_1_1 = tile_1_0_0.pixels(256*20 + 20)
    val pixel_1_2 = tile_1_0_0.pixels(236*256 - 20)
    val pixel_1_3 = tile_1_0_0.pixels(226*256 + 40)
    assert(pixel_1_1.blue > pixel_1_1.red, "zoom = 1, upper left conor")
    assert(pixel_1_2.blue > pixel_1_2.red, "zoom = 1, bottom right conor")
    assert(pixel_1_3.blue > pixel_1_2.red, "zoom = 1, bottom left conor")
  }
}
