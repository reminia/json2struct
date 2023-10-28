package json2struct

import json2struct.Printer.Syntax.toPrinterOps
import org.scalatest.matchers.should.Matchers._
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

    "convert struct type to map(json object)" in {
      val seq: Seq[Map[String, Any]] = Converter.convertStruct(
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

      noException shouldBe thrownBy {
        seq.foreach { m => println(m.print()) }
      }
    }
  }

}
