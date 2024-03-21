package json2struct

import json2struct.Printer.Syntax.toPrinterOps
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class ConverterSuite extends AnyWordSpec {

  "Converter" should {
    "convert json to struct type" in {
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
      val structs = Converter.convertJson(openai, "openAiResponse")
      structs.size should be(3)
      structs.map(_.name) should contain theSameElementsAs Seq("OpenAiResponse", "Choices", "Usage")
      noException shouldBe thrownBy {
        structs.foreach(s => println(s.print()))
      }
    }

    "convert multiple struct types to map(json object)" in {
      val seq: Seq[Any] = Converter.convertStruct(
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

      seq.size should be(1)

      // assert the top layer json
      val map: Map[String, Any] = seq.head.asInstanceOf[Map[String, Any]]
      map.keys should contain theSameElementsAs Seq("id", "object", "created", "model", "choices", "usage")

      // assert struct parsed correctly
      val usage: Map[String, Any] = map("usage").asInstanceOf[Map[String, Any]]
      usage.keys should contain theSameElementsAs Seq("prompt_tokens", "completion_tokens", "total_tokens")

      // assert array parsed correctly
      val choices: Seq[Map[String, Any]] = map("choices").asInstanceOf[Seq[Map[String, Any]]]
      choices.head.keys should contain theSameElementsAs Seq("index", "message", "finish_reason")

      noException shouldBe thrownBy {
        seq.foreach { m => println(m.print()) }
      }
    }

    "convert single struct type to json" in {
      val struct =
        """
          | type Data struct {
          |  Value int
          |  Type string
          | }
          |""".stripMargin
      val seq = Converter.convertStruct(struct)
      val map = seq.head.asInstanceOf[Map[String, Any]]
      map.keys should contain theSameElementsAs Seq("value", "type")
      noException shouldBe thrownBy {
        seq.foreach { m => println(m.print()) }
      }
    }

    "convert nested struct type to json" in {
      val struct =
        """
          |type Student struct {
          |  Name string
          |  Age int
          |  Address struct {
          |    Home string
          |    Office string
          |  }
          |}
          |""".stripMargin
      val seq = Converter.convertStruct(struct)
      val map = seq.head.asInstanceOf[Map[String, Any]]
      map.keys should contain theSameElementsAs Seq("address", "name", "age")
      noException shouldBe thrownBy {
        seq.foreach { m => println(m.print()) }
      }
    }
  }

}
