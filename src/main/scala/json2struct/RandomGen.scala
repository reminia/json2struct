package json2struct

import json2struct.GoStructAST.{Field, Struct, Tag}
import json2struct.GoType._
import org.scalacheck.Gen

import java.util
import scala.jdk.CollectionConverters.CollectionHasAsScala

// Generate value of O based on value of I
trait RandomGen[I, O] {
  def gen(i: I): O
}

object RandomGen {
  // todo: compose two Gen, maybe Kleisli
  def apply[I, O](f: I => O): RandomGen[I, O] = (i: I) => f(i)

  def struct2map(ss: Seq[Struct]): Seq[Map[String, Any]] = {
    val context = ss.map(s => s.name -> s).toMap
    val graph = Graph.from(ss)
    graph.sources.map(context.apply)
      .map(s => struct2map(s, context).sample.get)
      .toSeq
  }

  private def gotype2value(tpe: GoType, given: Map[String, Struct]): Gen[Any] = {
    tpe match {
      case GoInt => Gen.choose(0, 1000)
      case GoString => for {
        length <- Gen.choose(1, 8)
        chars <- Gen.listOfN(length, Gen.alphaNumChar)
      } yield chars.mkString
      case GoBool => Gen.oneOf(true, false)
      case GoInt32 => Gen.choose(1000, 100000)
      case GoUInt64 => Gen.choose(0L, Long.MaxValue)
      case GoFloat32 => Gen.double
      case GoArray(ele) => Gen.listOfN(3, gotype2value(ele, given))
      case GoStruct(name) =>
        given.get(name).fold[Gen[Any]](Gen.const(null))(s =>
          struct2map(s, given)
        )
      case Unknown => Gen.const(null)
    }
  }

  private def struct2map(s: Struct, given: Map[String, Struct]): Gen[Map[String, Any]] = {
    val seq: Seq[Gen[(String, Any)]] = s.fields.map { field =>
      for {
        k <- Gen.const(jsonKey(field))
        v <- gotype2value(field.tpe, given)
      } yield k -> v
    }
    val genSeq: Gen[util.ArrayList[(String, Any)]] = Gen.sequence(seq)
    genSeq.map(_.asScala).map(_.toMap)
  }

  def jsonKey(f: Field): String = {
    val Field(name, _, tag) = f
    tag match {
      case Tag.Simple(props) =>
        val values = props.get("json")
        values.fold(name) { seq =>
          val ret = seq.filter(x => !SPECIAL_JSON_PROPS.contains(x))
          ret.headOption.fold(name)(identity)
        }
      case _ => name
    }
  }

}
