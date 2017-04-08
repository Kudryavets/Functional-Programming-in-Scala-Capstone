package observatory

import java.time.LocalDate

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

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
    val sparkSession = SparkSession.builder()
      .master("local")
      .appName("temperature processing")
      .getOrCreate()

    val stationsStream = getClass.getResourceAsStream(stationsFile)
    val stationsSeq = scala.io.Source.fromInputStream(stationsStream).getLines.toSeq

    val temperaturesPath = getClass.getResource(temperaturesFile).getPath
    val temperatureDf = sparkSession.sparkContext.textFile(temperaturesPath)

    val result = locateTemperaturesImpl(sparkSession, year, stationsSeq, temperatureDf).collect()

    sparkSession.close()
    result
  }

  /**
    *
    * @param sparkSession SparkSession
    * @param year Year number
    * @param stationsSeq stations resource file content, every row contains ("stn", "wban", "lat", "lon")
    * @param temperatureRdd rdd[String] every line contains ("stn", "wban", "month", "day", "temperature")
    * @return Dataset[(LocalDate, Location, Double)]
    */
  def locateTemperaturesImpl(sparkSession: SparkSession,
                             year: Int, stationsSeq: Seq[String],
                             temperatureRdd: RDD[String]): RDD[(LocalDate, Location, Double)] = {
    val stationsMap = stationsSeq
      .map(_.split(','))
      .filter(splitted => splitted.length == 4) //  ignore data coming from stations that have no GPS coordinates
      .map(splitted => (splitted(0), splitted(1)) -> Location(splitted(2).toDouble, splitted(3).toDouble))
      .toMap

    val stationsMapBr = sparkSession.sparkContext.broadcast(stationsMap)

    temperatureRdd.flatMap { line =>
      val splitted = line.split(',')
      val stationsMap = stationsMapBr.value

      stationsMap.get((splitted(0), splitted(1))) match {
        case Some(location) =>
          Some((
            LocalDate.of(year, splitted(2).toInt, splitted(3).toInt),
            location,
            (splitted(4).toDouble - 32) * 5 / 9 // convert in degrees Celsius
            ))
        case _ => None
      }
    }
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Double)]): Iterable[(Location, Double)] = {
    ???
  }
}
