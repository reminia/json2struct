package json2struct

import json2struct.GoStructAST.{Field, Struct, Tag}
import json2struct.Printer.Syntax.toPrinterOps
import json2struct.Printer.{StructPrinter, upper}
import org.json4s.JsonAST.{JArray, JField, JObject}
import org.json4s._
import org.json4s.native.{JsonMethods, Serialization}

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
      .foreach(x => println(x.print()))

    convertStruct(
      """
        |type OpenAiResponse struct {
        |	Id      string   `json:"id"`
        |	Object  string   `json:"object"`
        |	Created uint64   `json:"created"`
        |	Model   string   `json:"model"`
        |	Choices []Choice `json:"choices"`
        |	Usage   Usage    `json:"usage"`
        |}
        |type Choice struct {
        |	Index        int     `json:"index"`
        |	Message      Message `json:"message"`
        |	FinishReason string  `json:"finish_reason"`
        |}
        |type Usage struct {
        |	PromptTokens     int `json:"prompt_tokens"`
        |	CompletionTokens int `json:"completion_tokens"`
        |	TotalTokens      int `json:"total_tokens"`
        |}
        |
        |type Message struct {
        |	Role    string `json:"role"`
        |	Content string `json:"content"`
        |}
        |
        |""".stripMargin)
      .foreach { m =>
        implicit val formats: Formats = Serialization.formats(NoTypeHints)
        println(Serialization.write(m))
      }
  }
}
