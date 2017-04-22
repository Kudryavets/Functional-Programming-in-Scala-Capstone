package observatory

import java.time.LocalDate

import observatory.Extraction._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSuite, Matchers}

import scala.collection.parallel.immutable.ParVector

@RunWith(classOf[JUnitRunner])
class ExtractionTest extends FunSuite with Matchers {

  test("locateTemperaturesImpl should perform processing correctly") {
    val stationsVec: ParVector[String] = ParVector(
      "010013,,,",
      "724017,03707,+37.358,-078.438",
      "724017,,+37.350,-078.433"
    )

    val temperatureVec: ParVector[String] = ParVector(
      "010013,,11,25,39.2",
      "724017,,08,11,81.14",
      "724017,03707,12,06,32",
      "724017,03707,01,29,35.6"
    )

    val resultVector = locateTemperaturesImpl(2015, stationsVec, temperatureVec)
      .map{ case (date, location, temp) => (date, location, BigDecimal(temp).setScale(1, BigDecimal.RoundingMode.HALF_UP).toDouble)}
      // workarround to check equality of doubles inside contain theSameElementsAs construction

    resultVector should have size 3
    resultVector should contain theSameElementsAs Seq(
      (LocalDate.of(2015, 8, 11), Location(37.35, -78.433), 27.3D),
      (LocalDate.of(2015, 12, 6), Location(37.358, -78.438), 0.0D),
      (LocalDate.of(2015, 1, 29), Location(37.358, -78.438), 2.0D)
      )
  }

  test("locationYearlyAverageRecords should perform processing correctly") {
    val records = Seq(
      (LocalDate.of(2015, 8, 11), Location(37.35, -78.433), 27.3D),
      (LocalDate.of(2015, 12, 6), Location(37.358, -78.438), 0.0D),
      (LocalDate.of(2015, 1, 29), Location(37.358, -78.438), 2.0D)
    )

    val result = locationYearlyAverageRecords(records)

    result should have size 2
    result should contain theSameElementsAs Seq(
      (Location(37.35, -78.433), 27.3),
      (Location(37.358, -78.438), 1.0)
    )
  }
}