package json2struct

import json2struct.StructAST.Tag.Simple
import json2struct.StructAST.{Field, Tag, Struct => AStruct}

import scala.util.parsing.combinator.JavaTokenParsers

/**
 * Parse Golang struct type to StructAST
 */
object StructParser extends JavaTokenParsers {

  val types: Parser[GoType] = ("int" | "int32" | "bool" | "float32" | "string" | ident) ^^ GoType.from

  val array: Parser[Field] = ident ~ "[]" ~ types ^^ {
    case name ~ _ ~ tpe => Field.Array(name, tpe)
  }

  val field: Parser[Field] = (ident ~ types) ^^ {
    case name ~ tpe => Field.Simple(name, tpe)
  } | array

  // TODO: fix it later
  val tag: Parser[Tag] = "`" ~ "json:" ~ "\"" ~> ident <~ "\"" ~ "`" ^^ Simple.apply

  val fieldWithTag: Parser[Field] = field ~ tag.? ^^ {
    case f ~ t if t.isDefined => f.setTag(t.get)
    case f ~ _ => f
  }

  val struct: Parser[AStruct] = ("type" ~> ident ~ "struct" ~ "{" ~ fieldWithTag.* <~ "}") ^^ {
    case name ~ _ ~ _ ~ seq => AStruct(name, seq)
  }

  def parse(input: String): Option[AStruct] = {
    parseAll(struct, input) match {
      case Success(res, _) => Some(res)
      case NoSuccess(msg, _) =>
        println(msg)
        None
    }
  }

  def main(args: Array[String]): Unit = {
    println(parse(
      """
        |type Usage struct {
        |    Completion_tokens int     `json:"completion_tokens"`
        |    Prompt_tokens int     `json:"prompt_tokens"`
        |    Total_tokens int     `json:"total_tokens"`
        |}
        |""".stripMargin))

    println(parse(
      """
        |type OpenAiResponse struct {
        |    Model string     `json:"model"`
        |    Choices []Choices     `json:"choices"`
        |    Usage Usage     `json:"usage"`
        |    Object string     `json:"object"`
        |    Id string     `json:"id"`
        |    Created int     `json:"created"`
        |}
        |""".stripMargin))
  }

}
