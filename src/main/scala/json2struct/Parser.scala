package json2struct

import json2struct.Struct.StructField
import org.json4s.JValue
import org.json4s.JsonAST.{JField, JObject}
import org.json4s.native.JsonMethods

object Parser {

  def parse(json: String, name: String): Seq[Struct] = {
    parse(JsonMethods.parse(json), name)
  }

  private def parse(json: JValue, name: String): Seq[Struct] = {
    json match {
      case JObject(fields) =>
        fields.map(parseJsonField)
        ???
      case _ => throw new IllegalArgumentException("input json must be JObject")
    }
  }


  private def parseJsonField(field: JField): StructField = {
    (field._1, GoType.from(field._2))
  }
}
