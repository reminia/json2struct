package json2struct

import json2struct.GoStructAST.{Field, Struct, Tag}
import json2struct.GoType.*
import json2struct.Printer.Syntax.toStringOps
import org.scalacheck.{Arbitrary, Gen}

import scala.collection.immutable.Seq
import scala.language.implicitConversions
import scala.util.control.TailCalls
import scala.util.control.TailCalls.{TailRec, done, tailcall}

object RandomGen {

  // Seq[Any] is actually Seq[Map[String, Any]]
  def genStructs(ss: Seq[Struct]): Seq[Any] = {
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
        case GoByte => Gen.choose(0, 255)
        case GoChar => Arbitrary.arbitrary[Char]
        case GoFloat32 => Arbitrary.arbitrary[Float]
        case GoArray(ele) =>
          tailcall(go(ele)).map(g => Gen.listOfN(3, g))
        case s: GoStruct =>
          struct2map(s).asInstanceOf[TailRec[Gen[Any]]]
        case GoAny => Gen.const(null)
      }
    }

    def struct2map(tpe: GoStruct): TailRec[Gen[Map[String, Any]]] = {
      given.get(tpe.name).fold[TailRec[Gen[Map[String, Any]]]](done(Gen.const(null))) {
        struct =>
          struct.fields
            .filter {
              _.tag match {
                case Tag.Simple(props)
                  if props.getOrElse(JSON_TAG, Seq.empty).contains(JSON_IGNORE) => false
                case _ => true
              }
            }
            .map { field =>
              tailcall(go(field.tpe)).map(
                _.map(v => jsonKey(field) -> v)
              )
            }
            .foldRight[TailRec[Gen[Map[String, Any]]]](done(Gen.const(Map.empty))) {
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
        val values = props.get(JSON_TAG)
        values.fold(name) { seq =>
          val ret = seq.filter(x => !SPECIAL_JSON_PROPS.contains(x))
          ret.headOption.fold(name)(identity)
        }
      case _ => name.lowerFst
    }
  }

}
