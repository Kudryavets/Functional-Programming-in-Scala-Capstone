package observatory


import org.junit.runner.RunWith
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers
import org.scalatest.{FunSuite, Matchers}

import scala.collection.parallel.immutable.ParVector

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
    val temperatures: ParVector[(Location, Double)] = ParVector(
          (Location(1D, 1D), 1D),
          (Location(1D, -1D), 2D),
          (Location(-1D, -1D), 3D),
          (Location(-10D, 10D), 60D)
        )

    val predicted: Double = predictTemperatureImpl(temperatures, Location(0D, 0D), DEFAULT_POWER_PARAMETER)

    assert(predicted === 2.194 +- 0.001)
  }

  test("predictTemperatureImpl neighbourhood") {
    val loc1 = Location(0, 90)
    val temp1 = 50
    val loc2 = Location(180, 90)
    val temp2 = 10

    val temperatures: ParVector[(Location, Double)] = ParVector(
      (loc1, temp1),
      (loc2, temp2)
    )

    val genLoc = Gen.resultOf(Location)

    forAll(genLoc) { loc: Location =>
      val predicted: Double = predictTemperatureImpl(temperatures, loc, DEFAULT_POWER_PARAMETER)
      if (distance(loc2Rad(loc), loc2Rad(loc1)) > distance(loc2Rad(loc), loc2Rad(loc2))) {
        temp1 - predicted > temp2 - predicted
      } else {
        temp1 - predicted <= temp2 - predicted
      }
    }
  }

//  test("basic interpolation test") {
//
//  }


}
