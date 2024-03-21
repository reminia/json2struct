package json2struct

import json2struct.GoStructAST.Struct
import json2struct.GoStructParser.{lineComment, multilineComment}
import org.scalatest.matchers.should.Matchers.*
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

    "parse single line comment" in {
      val input1 =
        """
          |// line comment
          |abcdef
          |""".stripMargin
      val res = parse(lineComment, input1)
      res should not be None
      res.get should include("line comment")
      res.get should not include "abc"

      val input2 =
        """////// abc
          |""".stripMargin
      parse(lineComment, input2).get should include("abc")
    }

    "parse multiline comment" in {
      val input =
        """
          |/**
          |  line comment
          |  hello world
          |*/
          |lalalala
          |""".stripMargin
      val res = parse(multilineComment, input)
      res should not be None
      res.get should include("line comment")
      res.get should include("hello world")
      res.get should not include "lala"

      val nested =
        """
          |/*
          |hi
          |/*
          |hello world
          |*/
          |""".stripMargin
      parse(multilineComment, nested) shouldBe None

      // go comment doesn't support nested style comment
      // but java support it
      val paired =
      """
        |/**
        |hi
        |/* hello world */
        |hi
        |**/
        |""".stripMargin
      parse(multilineComment, paired) shouldBe None
    }

    "parse struct type with comment" in {
      val struct =
        """
          |/*
          | token usage type
          |*/
          |type Usage struct {
          |              Completion_tokens int     `json:"completion_tokens"` // completion tokens
          |              Prompt_tokens int     `json:"prompt_tokens"`
          |              Total_tokens int     `json:"total_tokens"`
          |}
          |// end of the type
          |""".stripMargin
      val parseResult = GoStructParser.parse(struct)
      parseResult.isDefined shouldBe true
      val usage = parseResult.get.head
      usage.fields.map(_.name) should contain theSameElementsAs Seq("Completion_tokens", "Prompt_tokens", "Total_tokens")
    }

    "parse nested struct type" in {
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
      val parseResult = GoStructParser.parse(struct)
      parseResult.isDefined shouldBe true
      val structs = parseResult.get
      structs.size should be(1)
      val student = structs.head
      val address = student.fields.filter(_.isStruct).head.asInstanceOf[Struct]
      address.fields.map(_.name) should contain theSameElementsAs Seq("Home", "Office")
    }
  }

  def parse[T](p: GoStructParser.Parser[T], input: String): Option[T] = {
    GoStructParser.parse(p, input) match {
      case GoStructParser.Success(res, _) => Some(res)
      case _ => None
    }
  }

}
