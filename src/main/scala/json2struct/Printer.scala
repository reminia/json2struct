package json2struct

import json2struct.GoStructAST.{Field, Struct, Tag}
import json2struct.Printer.Syntax.toPrinterOps

import scala.language.implicitConversions

trait Printer[T] {
  def print(t: T): String
}

object Printer {

  def blank(n: Int = 1): String = " " * n

  def quote(str: String): String = "\"" + str + "\""

  def colon(l: String, r: String): String = l + ":" + r

  def backtick(in: String): String = "`" + in + "`"

  def indent(num: Int = 4): String = blank(num)

  def newline = "\n"

  // uppercase first char
  def upper(s: String): String = {
    s.toCharArray.toList match {
      case fst :: tail if fst.isLower =>
        (fst.toUpper :: tail).mkString
      case _ => s
    }
  }

  object Syntax {
    trait PrinterOps {
      def print(): String
    }

    implicit def toPrinterOps[T: Printer](t: T): PrinterOps =
      () => implicitly[Printer[T]].print(t)
  }


  implicit object TagPrinter extends Printer[Tag] {
    override def print(tag: Tag): String = tag match {
      case Tag.None => ""
      case Tag.Simple(props) =>
        val tags = props
          .map { case (k, v) => colon(k, quote(v.mkString(","))) }
          .mkString(blank())
        backtick(tags)
    }
  }

  implicit object FieldPrinter extends Printer[Field] {
    override def print(t: Field): String = {

      def jsonTag(): String = {
        t.tag match {
          case Tag.None => backtick(colon("json", quote(t.name)))
          case _ => t.tag.print()
        }
      }

      val sb = new StringBuilder()
      sb.append(indent())
        .append(t.name)
        .append(indent(2))
        .append(t.tpe.desc)
        .append(indent(2))
        .append(jsonTag())
      sb.toString
    }

  }

  implicit object StructPrinter extends Printer[Struct] {
    override def print(s: Struct): String = {
      val sb = new StringBuilder
      sb.append(s"type ${s.name} struct {")
        .append(s.fields.map(_.print()).mkString(newline, newline, newline))
        .append("}")
      sb.toString()
    }
  }

}