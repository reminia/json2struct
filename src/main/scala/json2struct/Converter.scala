package json2struct

import json2struct.GoStructAST.{Field, Struct, Tag}
import json2struct.Printer.Syntax.toPrinterOps
import json2struct.Printer.{StructPrinter, upper}
import org.json4s.JValue
import org.json4s.JsonAST.{JArray, JField, JObject}
import org.json4s.native.JsonMethods

import scala.collection.mutable

object Converter {

  def convertJson(json: String, name: String): Seq[Struct] = {
    convert(JsonMethods.parse(json), name)
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
      case _ => throw new IllegalArgumentException("input json must be a JObject")
    }

  }

  private def parseJsonField(field: JField): Field.Simple = {
    val (name, value) = field
    val _name = upper(name)
    val tag = Tag.Simple(Map("json" -> Seq(name)))
    Field.Simple(_name, GoType.apply(_name, value), tag)
  }

  def main(args: Array[String]): Unit = {
    val openai =
      """
        |{
        |  "id": "cmpl-uqkvlQyYK7bGYrRHQ0eXlWi7",
        |  "object": "text_completion",
        |  "created": 1589478378,
        |  "model": "gpt-3.5-turbo",
        |  "choices": [
        |    {
        |      "text": "\n\nThis is indeed a test",
        |      "index": 0,
        |      "logprobs": null,
        |      "finish_reason": "length"
        |    }
        |  ],
        |  "usage": {
        |    "prompt_tokens": 5,
        |    "completion_tokens": 7,
        |    "total_tokens": 12
        |  }
        |}""".stripMargin
    convertJson(openai, "openAiResponse")
      .map(_.print())
      .foreach(println)
  }
}
