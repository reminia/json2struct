package json2struct

import json2struct.GoStructAST.{Field, Struct, Tag}
import json2struct.GoStructParser.opt
import json2struct.GoType.GoArray

import scala.collection.mutable
import scala.util.parsing.combinator.JavaTokenParsers

/**
 * Parse Golang struct type to StructAST
 */
object GoStructParser extends JavaTokenParsers {

  lazy val goType: Parser[GoType] = ("int" | "int32" | "uint64" | "bool" | "float32" | "string" | ident) ^^ GoType.from

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

  lazy val array: Parser[Field] = ident ~ "[]" ~ goType ~ tag.? ^^ {
    case name ~ _ ~ tpe ~ t if t.nonEmpty => Field.Simple(name, GoArray(tpe), t.get)
    case name ~ _ ~ tpe ~ _ => Field.Simple(name, GoArray(tpe))
  }

  lazy val field: Parser[Field] = (ident ~ goType ~ tag.?) ^^ {
    case name ~ tpe ~ t if tpe.isStruct =>
      Field.Struct(name, t.fold[Tag](Tag.None)(identity))
    case name ~ tpe ~ t =>
      Field.Simple(name, tpe, t.fold[Tag](Tag.None)(identity))
  } | array

  def curly[T](in: Parser[T]): Parser[T] = "{" ~> in <~ "}"

  lazy val lineComment: Parser[String] = "//" ~ "[^\n]+".r.? ^^ {
    case _ ~ comment => comment.fold("")(identity)
  }

  lazy val multilineComment: Parser[String] = log("/*")("start") ~ log("(?s).+".r.?)("inside") ~ log("*/")("end") ^^ {
    case _ ~ comment ~ _ => comment.fold("")(identity)
  }

  lazy val struct: Parser[Struct] = ("type" ~> ident ~ "struct" ~ curly(field.*)) ^^ {
    case name ~ _ ~ seq => Struct(name, seq)
  }

  lazy val structs: Parser[Seq[Struct]] = struct.*

  def parse(input: String): Option[Seq[Struct]] = {
    parseAll(structs, input) match {
      case Success(res, _) => Some(res)
      case NoSuccess(msg, _) =>
        println(msg)
        None
    }
  }

}
