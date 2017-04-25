package observatory

import java.time.LocalDate

import scala.collection.parallel.immutable.ParVector

/**
  * 1st milestone: data extraction
  */
object Extraction {

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Int, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Double)] = {
    val stationsStream = getClass.getResourceAsStream(stationsFile)
    val stationsVec = scala.io.Source.fromInputStream(stationsStream).getLines.toVector.par

    val temperaturesPath = getClass.getResourceAsStream(temperaturesFile)
    val temperatureVec = scala.io.Source.fromInputStream(temperaturesPath).getLines.toVector.par

    locateTemperaturesImpl(year, stationsVec, temperatureVec)
  }

  /**
    *
    * @param year Year number
    * @param stationsVec stations resource file content, every row contains ("stn", "wban", "lat", "lon")
    * @param temperatureVec parVector[String] every line contains ("stn", "wban", "month", "day", "temperature")
    * @return Dataset[(LocalDate, Location, Double)]
    */
  def locateTemperaturesImpl(year: Int,
                             stationsVec: ParVector[String],
                             temperatureVec: ParVector[String]): Vector[(LocalDate, Location, Double)] = {
    val stationsMap = stationsVec
      .map(_.split(','))
      .filter(splitted => splitted.length == 4) //  ignore data coming from stations that have no GPS coordinates
      .map(splitted => (splitted(0), splitted(1)) -> Location(splitted(2).toDouble, splitted(3).toDouble))
      .toMap

    temperatureVec.flatMap { line =>
      val splitted = line.split(',')
      stationsMap.get((splitted(0), splitted(1))) match {
        case Some(location) =>
          Some((
            LocalDate.of(year, splitted(2).toInt, splitted(3).toInt),
            location,
            (splitted(4).toDouble - 32) * 5 / 9 // convert in degrees Celsius
          ))
        case _ => None
      }
    }.toVector
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Double)]): Iterable[(Location, Double)] = {
    records.toVector.par
      .aggregate(Map.empty[Location, (Double, Int)])(seqOperator, combOperator)
      .map{ case (location, (temp, count)) => (location, temp / count)}
  }

  private def seqOperator(accMap: Map[Location, (Double, Int)],
                    newValue: (LocalDate, Location, Double)): Map[Location, (Double, Int)] = {
    val oldValue = accMap.getOrElse(newValue._2, (0D, 0))
    accMap.updated(newValue._2, (oldValue._1 + newValue._3, oldValue._2 + 1))
  }

  private def combOperator(accMap1: Map[Location, (Double, Int)],
                   accMap2: Map[Location, (Double, Int)]): Map[Location, (Double, Int)] = {
    accMap1 ++ accMap2.map { case (location, (temp, count)) =>
      val oldValue = accMap1.getOrElse(location, (0D, 0))
      (location, (temp + oldValue._1, count + oldValue._2))
    }
  }
}