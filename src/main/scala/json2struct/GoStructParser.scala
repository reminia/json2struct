package json2struct

import json2struct.GoStructAST.{Field, Tag, Struct => AStruct}

import scala.collection.mutable
import scala.util.parsing.combinator.JavaTokenParsers

/**
 * Parse Golang struct type to StructAST
 */
object GoStructParser extends JavaTokenParsers {

  lazy val types: Parser[GoType] = ("int" | "int32" | "bool" | "float32" | "string" | ident) ^^ GoType.from

  def quote[T](inside: Parser[T]): Parser[T] = "\"" ~> inside <~ "\""

  lazy val csv: Parser[Seq[String]] = (ident ~ ("," ~> ident).*) ^^ {
    case name ~ seq => name :: seq
  }

  lazy val pair: Parser[(String, Seq[String])] = ident ~ ":" ~ quote(csv) ^^ {
    case key ~ _ ~ values => key -> values
  }

  lazy val tag: Parser[Tag] = "`" ~ pair.* <~ "`" ^^ {
    case _ ~ seq if seq.isEmpty => Tag.None
    case _ ~ seq =>
      var map = mutable.Map.empty[String, Seq[String]]
      seq.foreach {
        case (key, values) =>
          val seq = map.getOrElse(key, Seq.empty)
          map += key -> (seq ++ values)
      }
      Tag.Simple(map.toMap)
  }

  lazy val array: Parser[Field] = ident ~ "[]" ~ types ~ tag.? ^^ {
    case name ~ _ ~ tpe ~ t if t.nonEmpty => Field.Array(name, tpe, t.get)
    case name ~ _ ~ tpe ~ _ => Field.Array(name, tpe)
  }

  lazy val field: Parser[Field] = (ident ~ types ~ tag.?) ^^ {
    case name ~ tpe ~ t if tpe.isStruct =>
      Field.Struct(name, t.fold[Tag](Tag.None)(identity))
    case name ~ tpe ~ t =>
      Field.Simple(name, tpe, t.fold[Tag](Tag.None)(identity))
  } | array

  lazy val struct: Parser[AStruct] = ("type" ~> ident ~ "struct" ~ "{" ~ field.* <~ "}") ^^ {
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
        |type OpenAiResponse struct {
        |    Model string     `json:"model,omitempty" xml:"model"`
        |    Choices []Choices     `json:"choices"`
        |    Usage Usage     `json:"usage"`
        |    Object string     `json:"object"`
        |    Id string     `json:"id"`
        |    Created int     `json:"created"`
        |}
        |""".stripMargin))
  }

}
