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
    val temperatureRdd = sparkSession.sparkContext.textFile(temperaturesPath)

    val result = locateTemperaturesImpl(sparkSession, year, stationsSeq, temperatureRdd).collect()

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
                             year: Int,
                             stationsSeq: Seq[String],
                             temperatureRdd: RDD[String]): RDD[(LocalDate, Location, Double)] = {
    val stationsMap = stationsSeq
      .map(_.split(','))
      .filter(splitted => splitted.length == 4) //  ignore data coming from stations that have no GPS coordinates
      .map(splitted => (splitted(0), splitted(1)) -> Location(splitted(2).toDouble, splitted(3).toDouble))
      .toMap

    val stationsMapBr = sparkSession.sparkContext.broadcast(stationsMap)

    temperatureRdd.mapPartitions{ iterline =>
      val stationsMap = stationsMapBr.value

      iterline.flatMap { line =>
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
      }
    }
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

  def seqOperator(accMap: Map[Location, (Double, Int)],
                    newValue: (LocalDate, Location, Double)): Map[Location, (Double, Int)] = {
    val oldValue = accMap.getOrElse(newValue._2, (0D, 0))
    accMap.updated(newValue._2, (oldValue._1 + newValue._3, oldValue._2 + 1))
  }

  def combOperator(accMap1: Map[Location, (Double, Int)],
                   accMap2: Map[Location, (Double, Int)]): Map[Location, (Double, Int)] = {
    accMap1 ++ accMap2.map { case (location, (temp, count)) =>
      val oldValue = accMap1.getOrElse(location, (0D, 0))
      (location, (temp + oldValue._1, count + oldValue._2))
    }
  }
}