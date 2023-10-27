package json2struct

import json2struct.GoStructAST.{Field, Struct, Tag}
import json2struct.Printer.upper
import org.json4s.JsonAST.{JArray, JField, JObject}
import org.json4s._
import org.json4s.native.JsonMethods

import scala.collection.mutable

object Converter {

  def convertJson(json: String, name: String): Seq[Struct] = {
    convert(JsonMethods.parse(json), name)
  }

  def convertStruct(struct: String): Seq[Map[String, Any]] = {
    GoStructParser
      .parse(struct)
      .fold[Seq[Map[String, Any]]](Seq.empty) {
        ss => RandomGen.struct2map(ss)
      }
  }

  private def convert(json: JValue, name: String): Seq[Struct] = {
    def go(name: String, jObj: JObject, seq: mutable.Builder[Struct, Seq[Struct]]): Unit = {
      val struct = Struct(upper(name), jObj.obj.map(parseJsonField))
      seq += struct
      jObj.obj.foreach {
        case (name, obj: JObject) => go(name, obj, seq)
        case (name, _@JArray(arr)) if arr.nonEmpty =>
          arr.head match {
            case obj: JObject => go(name, obj, seq)
            case _ => ()
          }
        case _ => ()
      }
    }

    json match {
      case obj: JObject =>
        val seq = Seq.newBuilder[Struct]
        go(name, obj, seq)
        seq.result()
      case _ => throw new IllegalArgumentException("input json must be a json object")
    }
  }

  private def parseJsonField(field: JField): Field.Simple = {
    val (name, value) = field
    val _name = upper(name)
    val tag = Tag.Simple(Map("json" -> Seq(name)))
    Field.Simple(_name, GoType.apply(_name, value), tag)
  }
}
