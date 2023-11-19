package json2struct

import json2struct.GoStructAST.Struct
import json2struct.GoType.{GoArray, GoInt, GoStruct}
import org.scalacheck.Gen
import org.scalatest.PrivateMethodTester
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.immutable.Seq
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
            val _struct = struct.copy(fields = Seq(new Struct(name)))
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

  "RandomGen" should {
    "ignore struct field with json '-' tag" in {
      val struct =
        """
          |type Person struct {
          |	ID   int    `json:"id"`
          |	Name string `json:"name"`
          |	Age int `json:"-"`
          |}
          |""".stripMargin
      val structs = RandomGen.genStructs(GoStructParser.parse(struct).get)
      val anyMap = structs.head.asInstanceOf[Map[String, Any]]
      anyMap.size shouldBe 2
      Seq("age", "Age") foreach { ele =>
        anyMap.keys should not contain ele
      }
    }
  }
}