package json2struct

import json2struct.GoStructAST.{Field, Struct, Tag}
import json2struct.GoType._
import org.json4s.JsonAST.{JBool, JDecimal, JObject}
import org.json4s.{JArray, JInt, JString, JValue}
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

  case class GoStructGenerator(ctx: Map[String, JValue]) extends RandomGen[GoType, JValue] {
    override def gen(tpe: GoType): JValue = tpe match {
      case GoInt => JInt(1)
      case GoBool => JBool.True
      case GoInt32 => JInt(1)
      case GoFloat32 => JDecimal(null)
      case GoString => JString("")
      case GoArray(ele) => JArray(List.empty)
      case GoStruct(name) => ctx.getOrElse(name, JObject())
    }
  }

  def gotype2value(tpe: GoType, given: Map[String, Struct]): Gen[Any] = {
    tpe match {
      case GoInt => Gen.choose(0, 1000)
      case GoString => for {
        length <- Gen.choose(1, 8)
        chars <- Gen.listOfN(length, Gen.alphaNumChar)
      } yield chars.mkString
      case GoBool => Gen.oneOf(true, false)
      case GoInt32 => Gen.choose(1000, 100000)
      case GoFloat32 => Gen.double
      case GoArray(ele) => Gen.listOfN(3, gotype2value(ele, given))
      case GoStruct(name) => struct2map(given(name), given)
      case Unknown => Gen.const(null)
    }
  }

  def struct2map(s: Struct): Map[String, Any] =
    struct2map(s, Map.empty).sample.get

  def struct2map(ss: Seq[Struct]): Map[String, Any] = {
    null
  }

  def struct2map(s: Struct, given: Map[String, Struct]): Gen[Map[String, Any]] = {
    val seq: Seq[Gen[(String, Any)]] = s.fields.map { field =>
      field.tag
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
          val ret = seq.filter(x => !Seq("-", "omitempty").contains(x))
          ret.headOption.fold(name)(identity)
        }
      case _ => name
    }
  }

  def main(args: Array[String]): Unit = {
    GoStructParser.parse(
        """
          |type Usage struct {
          |    Completion_tokens int     `json:"completion_tokens"`
          |    Prompt_tokens int     `json:"prompt_tokens"`
          |    Total_tokens int     `json:"total_tokens"`
          |}
          |""".stripMargin)
      .map(_.head)
      .map(struct2map)
      .foreach(println)
  }
}
