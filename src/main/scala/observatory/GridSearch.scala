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

import scala.collection.parallel.immutable.{ParMap, ParVector}

class GridSearch(temperatures: Iterable[(Location, Double)]) {
  import GridSearch._
  lazy val grid: ParMap[Location, Double] = locationsNet.map(loc => loc -> Visualization.predictTemperature(temperatures, loc)).toMap

  def apply(loc: Location): Double = grid(loc)
}

object GridSearch {
  val locationsNet: ParVector[Location] = (for {
    lat <- -89 to 90
    lon <- -180 to 179
  } yield Location(lat, lon)
    ).toVector.par
}
