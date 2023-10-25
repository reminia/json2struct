package json2struct

import json2struct.StructAST.Tag.Simple
import json2struct.StructAST.{Field, Tag, Struct => AStruct}

import scala.util.parsing.combinator.JavaTokenParsers

/**
 * Parse Golang struct type to StructAST
 */
object GoStructParser extends JavaTokenParsers {

  val types: Parser[GoType] = ("int" | "int32" | "bool" | "float32" | "string" | ident) ^^ GoType.from

  // TODO: fix it later
  val tag: Parser[Tag] = "`" ~ "json:" ~ "\"" ~> ident <~ "\"" ~ "`" ^^ Simple.apply

  val array: Parser[Field] = ident ~ "[]" ~ types ~ tag.? ^^ {
    case name ~ _ ~ tpe ~ t if t.nonEmpty => Field.Array(name, tpe, t.get)
    case name ~ _ ~ tpe ~ _ => Field.Array(name, tpe)
  }

  val field: Parser[Field] = (ident ~ types ~ tag.?) ^^ {
    case name ~ tpe ~ t if tpe.isStruct =>
      Field.Struct(name, t.fold[Tag](Tag.None)(x => x))
    case name ~ tpe ~ t =>
      Field.Simple(name, tpe, t.fold[Tag](Tag.None)(x => x))
  } | array

  val struct: Parser[AStruct] = ("type" ~> ident ~ "struct" ~ "{" ~ field.* <~ "}") ^^ {
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
