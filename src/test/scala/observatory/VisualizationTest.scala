package observatory


import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers
import org.scalatest.{FunSuite, Matchers}

@RunWith(classOf[JUnitRunner])
class VisualizationTest extends FunSuite with Checkers with Matchers {
  import Visualization._

  test("basic distance test") {
    assert(distance(loc2Rad(Location(0, 90)), loc2Rad(Location(180, 90))) === math.Pi +- .0001)
    assert(distance(loc2Rad(Location(90, 0)), loc2Rad(Location(-90, 0))) === math.Pi +- .0001)
    assert(distance(loc2Rad(Location(180, 90)), loc2Rad(Location(180, 90))) === 0D +- .0001)
    assert(distance(loc2Rad(Location(180, 90)), loc2Rad(Location(0, -90))) === 0D +- .0001)
  }

  test("basic predictTemperatureImpl test") {
    val temperatures: Vector[(Location, Double)] = Vector(
          (Location(1D, 1D), 1D),
          (Location(1D, -1D), 2D),
          (Location(-1D, -1D), 3D),
          (Location(-10D, 10D), 60D)
        )

    val predicted: Double = predictTemperatureImpl(temperatures, Location(0D, 0D), DEFAULT_POWER_PARAMETER)

    assert(predicted === 2.194 +- 0.001)
  }

  test("interpolation tests") {
    val colors = Seq(
      (60D,	Color(255,	255,	255)),
      (32D,	Color(255,	0,	0)),
      (0D,	Color(0,	255,	255)),
      (12D,	Color(255,	255,	0)),
      (-15D,	Color(0,	0,	255)),
      (-27D,	Color(255,	0,	255)),
      (-50D,	Color(33,	0,	107)),
      (-60D,	Color(0,	0,	0))
    )

    assert(interpolateColor(colors, 32) === Color(255,	0,	0))
    assert(interpolateColor(colors, 70) === Color(255,	255,	255))
    assert(interpolateColor(colors, -70) === Color(0,	0,	0))
    assert(interpolateColor(colors, 20) === Color(255,	153,	0))
    assert(interpolateColor(colors, -32) === Color(207,	0,	223))
  }
}
