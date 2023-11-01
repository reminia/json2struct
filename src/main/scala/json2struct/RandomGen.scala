package json2struct

import json2struct.GoStructAST.{Field, Struct, Tag}
import json2struct.GoType._
import org.scalacheck.Gen

import scala.collection.immutable.Seq
import scala.language.implicitConversions
import scala.util.control.TailCalls
import scala.util.control.TailCalls.{TailRec, done, tailcall}

object RandomGen {

  def struct2map(ss: Seq[Struct]): Seq[Any] = {
    val context = ss.map(s => s.name -> s).toMap
    val graph = Graph.from(ss)
    graph.sources.map(context.apply)
      .map(s => gotype2value(GoStruct(s.name), context).sample.get)
      .toSeq
  }

  private def gotype2value(tpe: GoType, given: Map[String, Struct]): Gen[Any] = {

    implicit def toTailRec[A](a: A): TailRec[A] = TailCalls.done(a)

    def go(tpe: GoType): TailRec[Gen[Any]] = {
      tpe match {
        case GoInt => Gen.choose(0, 1000)
        case GoString => for {
          length <- Gen.choose(1, 8)
          chars <- Gen.listOfN(length, Gen.alphaNumChar)
        } yield chars.mkString
        case GoBool => Gen.oneOf(true, false)
        case GoInt32 => Gen.choose(1000, 100000)
        case GoUInt64 => Gen.choose(0L, Long.MaxValue)
        case GoFloat32 => Gen.double.map(_.toFloat)
        case GoArray(ele) =>
          tailcall(go(ele)).map(g => Gen.listOfN(3, g))
        case s@GoStruct(_) =>
          struct2map(s).asInstanceOf[TailRec[Gen[Any]]]
        case Unknown => Gen.const(null)
      }
    }

    def struct2map(tpe: GoStruct): TailRec[Gen[Map[String, Any]]] = {
      given.get(tpe.name).fold[TailRec[Gen[Map[String, Any]]]](done(Gen.const(null))) {
        struct =>
          val seq = struct.fields.map { field =>
            tailcall(go(field.tpe)).map(
              _.map(v => jsonKey(field) -> v)
            )
          }
          seq.foldRight[TailRec[Gen[Map[String, Any]]]](done(Gen.const(Map.empty))) {
            (trTuple, trMap) =>
              for {
                genMap <- trMap
                genTuple <- trTuple
              } yield {
                genMap.flatMap(map => genTuple.map(tuple => map + tuple))
              }
          }
      }
    }

    go(tpe).result
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
