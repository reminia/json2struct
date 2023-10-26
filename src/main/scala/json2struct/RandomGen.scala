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

  def gotype2value(tpe: GoType, given: Map[String, Struct]): Gen[Any] = {
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
      case GoArray(ele) => Gen.nonEmptyListOf(3, gotype2value(ele, given))
      case GoStruct(name) =>
        given.get(name).fold[Gen[Any]](Gen.const(null))(s =>
          struct2map(s, given)
        )
      case Unknown => Gen.const(null)
    }
  }

  def struct2map(s: Struct): Map[String, Any] =
    struct2map(s, Map.empty).sample.get

  def struct2map(ss: Seq[Struct]): Seq[Map[String, Any]] = {
    val context = ss.map(s => s.name -> s).toMap
    val nodes = Set.newBuilder[String]
    val edges = Set.newBuilder[(String, String)]
    ss.foreach { s =>
      s.fields.foreach {
        case f: Field.Struct =>
          edges += s.name -> f.name
          nodes += s.name += f.name
        case f: Field.Array if f.tpe.isStruct =>
          edges += s.name -> f.tpe.desc
          nodes += s.name += f.tpe.desc
        case _ => ()
      }
    }
    val graph = Graph(nodes.result(), edges.result())
    println(graph)
    graph.sources.map(context.apply)
      .map(s => struct2map(s, context).sample.get)
      .toSeq
  }

  def struct2map(s: Struct, given: Map[String, Struct]): Gen[Map[String, Any]] = {
    val seq: Seq[Gen[(String, Any)]] = s.fields.map { field =>
      field.tag
      for {
        k <- Gen.const(field.name)
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
