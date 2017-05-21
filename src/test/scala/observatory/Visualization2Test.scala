package observatory

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers

@RunWith(classOf[JUnitRunner])
class Visualization2Test extends FunSuite with Checkers {
  import Visualization2._

  test("basic bilinearInterpolation test") {
    val predicted = bilinearInterpolation(0.5, 0.5, 10, 0, 0, -10)

    assert(predicted === 0)
  }
}
