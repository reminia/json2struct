package json2struct

import com.typesafe.config.ConfigFactory
import json2struct.Conf.APP_CONF
import json2struct.GoStructAST.Struct
import json2struct.GoType.{GoArray, GoInt, GoStruct}
import org.scalacheck.Gen
import org.scalatest.PrivateMethodTester
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.immutable.Seq
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

class RandomGenSuite extends AnyWordSpec with PrivateMethodTester {

  "RandomGen" when {

    val method = PrivateMethod[Gen[Any]](Symbol("gotype2value"))

    "generate data of deep nested GoArray" should {
      "not stackoverflow" in {
        val deepArray: GoType = (1 to 10000).foldLeft[GoType](GoInt) {
          (acc, _) => GoArray(acc)
        }
        noException shouldBe thrownBy {
          new RandomGen(APP_CONF) invokePrivate method(deepArray, Map.empty)
        }
      }
    }

    "generate data of deep nested GoStruct" should {
      "not stackoverflow" in {
        val struct = Struct("Root", Seq.empty)
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
          new RandomGen(APP_CONF) invokePrivate method(GoStruct("Root"), map.toMap)
        }
      }
    }
  }

  "RandomGen" should {
    val struct =
      """
        |type Person struct {
        |	ID   int    `json:"id"`
        |	Name string `json:"name"`
        |	Age int `json:"-"`
        | FavoriteMovie String
        | }
        |""".stripMargin

    "ignore struct field with json '-' tag" in {
      val structs = new RandomGen(APP_CONF)
        .genStructs(GoStructParser.parse(struct).get)
      val anyMap = structs.head.asInstanceOf[Map[String, Any]]
      anyMap.size shouldBe 3
      Seq("age", "Age") foreach { ele =>
        anyMap.keys should not contain ele
      }
    }

    "produce snake case json prop if snake-case enabled" in {
      val conf = ConfigFactory.parseMap(Map("struct2json.snake-case" -> true).asJava)
      val structs = new RandomGen(conf)
        .genStructs(GoStructParser.parse(struct).get)
      val anyMap = structs.head.asInstanceOf[Map[String, Any]]
      anyMap.keys should contain("favorite_movie")
    }
  }

}