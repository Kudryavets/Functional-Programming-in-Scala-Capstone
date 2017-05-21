/*
 * Copyright (c) 2014, CleverDATA, LLC. All Rights Reserved.
 *
 * All information contained herein is, and remains the property of CleverDATA, LLC.
 * The intellectual and technical concepts contained herein are proprietary to
 * CleverDATA, LLC. Dissemination of this information or reproduction of this
 * material is strictly forbidden unless prior written permission is obtained from
 * CleverDATA, LLC.
 *
 * Unless required by applicable law or agreed to in writing, software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 */

package observatory

import observatory.Visualization.{DEFAULT_POWER_PARAMETER, distance, loc2Rad, predictTemperatureImpl}
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}

import scala.math.abs

object VisualizationTest1_2 extends Properties("Visualisation test") {
  val loc1 = Location(0, 90)
  val temp1 = 50
  val loc2 = Location(179, 90)
  val temp2 = -50

  val temperatures: Vector[(Location, Double)] = Vector(
    (loc1, temp1),
    (loc2, temp2)
  )

  val genLoc = Gen.resultOf(Location)

  property("test grid") = forAll(genLoc) { loc: Location =>
    val predicted: Double = predictTemperatureImpl(temperatures, loc, DEFAULT_POWER_PARAMETER)
    val dist1 = distance(loc2Rad(loc), loc2Rad(loc1))
    val dist2 = distance(loc2Rad(loc), loc2Rad(loc2))

    if (dist1 > dist2) {
      abs(temp1 - predicted) > abs(temp2 - predicted)
    } else {
      abs(temp1 - predicted) <= abs(temp2 - predicted)
    }
  }
}
