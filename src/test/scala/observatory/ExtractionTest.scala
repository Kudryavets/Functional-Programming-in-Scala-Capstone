package observatory

import java.time.LocalDate

import observatory.Extraction._
import org.apache.spark.rdd.RDD
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSuite, Matchers}

@RunWith(classOf[JUnitRunner])
class ExtractionTest extends FunSuite with SparkSqlContext with Matchers {

  test("locateTemperaturesImpl should perform processing correctly") {
    val sqlContext = sparkSession.sqlContext

    val stationsDf: Seq[String] = Seq (
      "010013,,,",
      "724017,03707,+37.358,-078.438",
      "724017,,+37.350,-078.433"
    )

    val temperatureRdd: RDD[String] = sparkSession.sparkContext.parallelize(
      Seq(
        "010013,,11,25,39.2",
        "724017,,08,11,81.14",
        "724017,03707,12,06,32",
        "724017,03707,01,29,35.6"
      )
    )

    val resultArray = locateTemperaturesImpl(sparkSession, 2015, stationsDf, temperatureRdd)
      .map{ case (date, location, temp) => (date, location, BigDecimal(temp).setScale(1, BigDecimal.RoundingMode.HALF_UP).toDouble)}
      // workarround to check equality of doubles inside contain theSameElementsAs construction
      .collect()

    resultArray should have size 3
    resultArray should contain theSameElementsAs Seq(
      (LocalDate.of(2015, 8, 11), Location(37.35, -78.433), 27.3D),
      (LocalDate.of(2015, 12, 6), Location(37.358, -78.438), 0.0D),
      (LocalDate.of(2015, 1, 29), Location(37.358, -78.438), 2.0D)
      )
  }
}