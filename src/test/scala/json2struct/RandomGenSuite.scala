package json2struct

import json2struct.GoStructAST.{Field, Struct}
import json2struct.GoType.{GoArray, GoInt, GoStruct}
import org.scalacheck.Gen
import org.scalatest.PrivateMethodTester
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.mutable

class RandomGenSuite extends AnyWordSpec with PrivateMethodTester {

  "RandomGen" when {

    val method = PrivateMethod[Gen[Any]](Symbol("gotype2value"))

    "generate data of deep nested GoArray" should {
      "not stackoverflow" in {
        val deepArray: GoType = (1 to 10000).foldLeft[GoType](GoInt) {
          (acc, _) => GoArray(acc)
        }
        noException shouldBe thrownBy {
          RandomGen invokePrivate method(deepArray, Map.empty)
        }
      }
    }

    "generate data of deep nested GoStruct" should {
      "not stackoverflow" in {
        val struct = Struct("root", Seq.empty)
        val map = mutable.HashMap.empty[String, Struct]
        val gen = Gen.alphaStr
        (1 to 10000).foldLeft[Struct](struct) {
          (struct, _) =>
            var name = gen.sample.get
            while (map.contains(name)) {
              name = gen.sample.get
            }
            val _struct = struct.copy(fields = Seq(Field.Struct(name)))
            map += _struct.name -> _struct
            val res = Struct(name, Seq.empty)
            map += name -> res
            res
        }
        noException shouldBe thrownBy {
          RandomGen invokePrivate method(GoStruct("root"), map.toMap)
        }
      }
    }
  }
}