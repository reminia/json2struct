package json2struct

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class GoStructParserSuite extends AnyWordSpec {

  "Go struct parser" should {
    "parse openai struct types" in {
      val option = GoStructParser.parse(
        """
          |
          |type OpenAiResponse struct {
          |	Id      string   `json:"id"`
          |	Object  string   `json:"object"`
          |	Created uint64   `json:"created"`
          |	Model   string   `json:"model"`
          |	Choices []Choice `json:"choices"`
          |	Usage   Usage    `json:"usage"`
          |}
          |
          |type Usage struct {
          |    Completion_tokens int     `json:"completion_tokens"`
          |    Prompt_tokens int     `json:"prompt_tokens"`
          |    Total_tokens int     `json:"total_tokens"`
          |}
          |
          |""".stripMargin)

      val structs = option.get
      structs.length should be(2)
      val map = structs.map(s => s.name -> s).toMap
      val choices = map("OpenAiResponse").fields.filter(_.name == "Choices")
      choices.head.isArray should be(true)
      val usage = map("OpenAiResponse").fields.filter(_.name == "Usage")
      usage.head.isStruct should be(true)
    }
  }

}
