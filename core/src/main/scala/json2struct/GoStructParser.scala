package json2struct

import json2struct.GoStructAST.{Field, Struct, Tag}
import json2struct.GoType.GoArray

import scala.collection.mutable
import scala.util.parsing.combinator.JavaTokenParsers

/**
 * Parse Golang struct type to StructAST
 */
object GoStructParser extends JavaTokenParsers {

  lazy val goType: Parser[GoType] =
    (
      "int" | "int32" | "uint64" | "float32"
        | "rune" | "byte"
        | "bool" | "string"
        | "any"
        | ident
      ) ^^ GoType.from

  def quote[T](inside: Parser[T]): Parser[T] = "\"" ~> inside <~ "\""

  lazy val tagProp: Parser[String] = ident | "-"

  lazy val csv: Parser[Seq[String]] = (tagProp ~ ("," ~> tagProp).*) ^^ {
    case name ~ seq => name :: seq
  }

  lazy val pair: Parser[(String, Seq[String])] = ident ~ ":" ~ quote(csv) ^^ {
    case key ~ _ ~ values => key -> values
  }

  lazy val tag: Parser[Tag] = "`" ~ pair.* <~ "`" ^^ {
    case _ ~ seq if seq.isEmpty => Tag.None
    case _ ~ seq =>
      val map = mutable.Map.empty[String, Seq[String]]
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
      Struct(name, Seq(), t.fold[Tag](Tag.None)(identity))
    case name ~ tpe ~ t =>
      Field.Simple(name, tpe, t.fold[Tag](Tag.None)(identity))
  } | array

  def curly[T](in: Parser[T]): Parser[T] = "{" ~> in <~ "}"

  lazy val lineComment: Parser[String] = log("//.*".r)("single")
  lazy val multiline: Parser[String] = log("""/\*.*(\n*.*)*\*/""".r)("multiline")
  lazy val nested: Parser[String] = """/\*.*(\n*.*)*/\*""".r
  lazy val multilineComment: Parser[String] = multiline - log(nested)("nest")

  lazy val comment = lineComment | multilineComment

  lazy val _field = comment.* ~> field <~ comment.*

  lazy val struct: Parser[Struct] = ("type" ~> ident ~ "struct" ~ curly(_field.*)) ^^ {
    case name ~ _ ~ seq => Struct(name, seq)
  }

  lazy val _struct = comment.* ~> struct <~ comment.*

  lazy val structs: Parser[Seq[Struct]] = _struct.*

  def parse(input: String): Option[Seq[Struct]] =
    parseAll(structs, input) match {
      case Success(res, _) => Some(res)
      case NoSuccess(msg, _) =>
        println(msg)
        None
    }

}
