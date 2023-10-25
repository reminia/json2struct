package json2struct

import json2struct.GoStructAST.{Field, Struct, Tag}

trait Printer[T] {
  def print(t: T): String
}

object Printer {

  def blank(n: Int = 1): String = " " * n

  def quote(str: String): String = "\"" + str + "\""

  def colon(l: String, r: String): String = l + ":" + r

  def backtick(in: String): String = "`" + in + "`"

  def indent(num: Int = 4): String = blank(4)

  def newline = "\n"

  // uppercase first char
  def upper(s: String): String = {
    s.toCharArray.toList match {
      case fst :: tail if fst.isLower =>
        (fst.toUpper :: tail).mkString
      case _ => s
    }
  }

  object TagPrinter extends Printer[Tag] {
    override def print(tag: Tag): String = tag match {
      case Tag.None => ""
      case Tag.Simple(props) =>
        val tags = props.map { case (k, v) => colon(k, quote(v.mkString(","))) }
          .mkString(blank())
        backtick(tags)
    }
  }

  object FieldPrinter extends Printer[Field] {
    override def print(t: Field): String = {

      def jsonTag(): String = {
        t.tag match {
          case Tag.None => backtick(colon("json", quote(t.name)))
          case _ => TagPrinter.print(t.tag)
        }
      }

      val sb = new StringBuilder()
      sb.append(indent())
        .append(upper(t.name))
        .append(indent(2))
        .append(t.tpe.desc)
        .append(indent(2))
        .append(jsonTag())
      sb.toString
    }

  }

  object StructPrinter extends Printer[Struct] {
    override def print(s: Struct): String = {
      val sb = new StringBuilder
      sb.append(s"type ${upper(s.name)} struct {")
        .append(s.fields.map(FieldPrinter.print).mkString(newline, newline, newline))
        .append("}")
      sb.toString()
    }
  }

}
