package observatory

import org.scalacheck.{Gen, Prop, Properties}
import Visualization._
import math._

object ManipulationTest extends Properties("Manipulation test") {
  val loc1 = Location(-89, -180)
  val temp1 = -50
  val loc2 = Location(90, 179)
  val temp2 = 50

  val temperatures: Vector[(Location, Double)] = Vector(
    (loc1, temp1),
    (loc2, temp2)
  )

  val genlat = for (lat <- Gen.choose(-89, 90)) yield lat
  val genLon = for (lon <- Gen.choose(-180, 179)) yield lon

  val gridFunc = Manipulation.makeGrid(temperatures)

  property("test grid") = Prop.forAll(genlat, genLon) { (lat: Int, lon: Int) =>
    val predicted = gridFunc(lat, lon)
    val loc = Location(lat, lon)
    val dist1 = distance(loc2Rad(loc), loc2Rad(loc1))
    val dist2 = distance(loc2Rad(loc), loc2Rad(loc2))

    if (distance(loc2Rad(loc), loc2Rad(loc1)) > distance(loc2Rad(loc), loc2Rad(loc2))) {
      abs(temp1 - predicted) > abs(temp2 - predicted)
    } else {
      abs(temp2 - predicted) >= abs(temp1 - predicted)
    }
  }
}