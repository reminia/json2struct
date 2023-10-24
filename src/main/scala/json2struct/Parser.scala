package json2struct

import json2struct.Struct.{ShowStruct, StructField}
import org.json4s.JValue
import org.json4s.JsonAST.{JArray, JField, JObject}
import org.json4s.native.JsonMethods

import scala.collection.mutable

object Parser {

  def parse(json: String, name: String): Seq[Struct] = {
    parse(JsonMethods.parse(json), name)
  }

  private def parse(json: JValue, name: String): Seq[Struct] = {

    def go(name: String, jObj: JObject, seq: mutable.Builder[Struct, Seq[Struct]]): Unit = {
      val struct = Struct(name, jObj.obj.map(parseJsonField))
      seq += struct
      jObj.obj.foreach {
        case (name, obj: JObject) => go(name, obj, seq)
        case (name, _@JArray(arr)) =>
          if (arr.isEmpty) ()
          else {
            arr.head match {
              case obj: JObject => go(name, obj, seq)
              case _ => ()
            }
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

  private def parseJsonField(field: JField): StructField = {
    (field._1, GoType(field._2, field._1))
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
    parse(openai, "openAiResponse")
      .map(ShowStruct.show)
      .foreach(println)
  }
}
