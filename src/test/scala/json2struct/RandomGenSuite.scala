package json2struct

import json2struct.GoType.{GoArray, GoInt}
import org.scalacheck.Gen
import org.scalatest.PrivateMethodTester
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class RandomGenSuite extends AnyWordSpec with PrivateMethodTester {

  "RandomGen" when {
    "generate data of deep nested GoType" should {
      "not stackoverflow" in {
        val deepArray: GoType = (1 to 10000).foldLeft[GoType](GoInt) {
          (acc, _) => GoArray(acc)
        }
        val method = PrivateMethod[Gen[Any]](Symbol("gotype2value"))
        noException shouldBe thrownBy {
          RandomGen invokePrivate method(deepArray, Map.empty)
        }
      }
    }
  }
}
