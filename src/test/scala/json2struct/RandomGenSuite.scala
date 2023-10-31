package json2struct

import json2struct.GoStructAST.{Field, Struct}
import json2struct.GoType.{GoArray, GoInt, GoStruct}
import json2struct.Printer.Syntax.toPrinterOps
import org.scalacheck.Gen
import org.scalatest.PrivateMethodTester
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import java.io.PrintWriter

class RandomGenSuite extends AnyWordSpec with PrivateMethodTester {

  "RandomGen" when {
    "generate data of deep nested GoArray" should {
      "not stackoverflow" in {
        val deepArray: GoType = (1 to 10000).foldLeft[GoType](GoInt) {
          (acc, _) => GoArray(acc)
        }
        val method = PrivateMethod[Gen[Any]](Symbol("gotype2value"))
        noException shouldBe thrownBy {
          val ret = RandomGen invokePrivate method(deepArray, Map.empty)
//                    println(ret.sample.get.print())
        }
      }
    }

    "generate data of deep nested GoStruct" should {
      "not stackoverflow" in {
        val struct = Struct("root", Seq.empty)
        val map = Map.newBuilder[String, Struct]
        var set = scala.collection.mutable.HashSet.empty[String]
        val gen = Gen.alphaStr
        (1 to 10000).foldLeft[Struct](struct) {
          (struct, _) =>
            var name = gen.sample.get
            while (set.contains(name)) {
              name = gen.sample.get
            }
            set.add(name)
            val _struct = struct.copy(fields = Seq(Field.Struct(name)))
            map += _struct.name -> _struct
            val res = Struct(name, Seq.empty)
            map += name -> res
            res
        }
        val method = PrivateMethod[Gen[Any]](Symbol("gotype2value"))
        noException shouldBe thrownBy {
          val ret = RandomGen invokePrivate method(GoStruct("root"), map.result())
//          ret.sample.get.print()
        }
      }
    }
  }
}