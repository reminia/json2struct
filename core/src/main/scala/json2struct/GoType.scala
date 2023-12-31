package json2struct

import json2struct.GoType.GoStruct
import json2struct.Printer.Syntax.toStringOps
import org.json4s.JsonAST.*
import org.json4s.{JArray, JBool}

import scala.annotation.switch

sealed trait GoType {
  def desc: String

  override def toString: String = desc

  def isStruct: Boolean = this match {
    case _: GoStruct => true
    case _           => false
  }

}

object GoType {
  // byte is unint8
  case object GoByte extends GoType {
    override def desc = "byte"
  }

  case object GoInt extends GoType {
    override def desc = "int"
  }

  case object GoInt32 extends GoType {
    override def desc = "int32"
  }

  case object GoUInt64 extends GoType {
    override def desc = "uint64"
  }

  case object GoFloat32 extends GoType {
    override def desc: String = "float32"
  }

  // GoChar is rune is int32
  case object GoChar extends GoType {
    override def desc: String = "rune"
  }

  case object GoString extends GoType {
    override def desc: String = "string"
  }

  case object GoBool extends GoType {
    override def desc: String = "bool"
  }

  case class GoArray(element: GoType) extends GoType {
    override def desc: String = s"[]${element.desc}"
  }

  case class GoStruct(name: String) extends GoType {
    override def desc: String =
      if (name.isEmpty)
        ANY
      else name.upperCamelCase
  }

  case object GoAny extends GoType {
    override def desc: String = ANY
  }

  def from(tpe: String): GoType =
    (tpe: @switch) match {
      case "int"     => GoInt
      case "int32"   => GoInt32
      case "uint64"  => GoUInt64
      case "rune"    => GoChar
      case "byte"    => GoByte
      case "float32" => GoFloat32
      case "string"  => GoString
      case "bool"    => GoBool
      case "any"     => GoAny
      // unknown types default to struct type
      case sth => GoStruct(sth)
    }

  def apply(name: String, value: JValue): GoType =
    value match {
      case JInt(_)                => GoInt
      case JDouble(_)             => GoFloat32
      case JLong(num) if num >= 0 => GoUInt64
      case JString(_)             => GoString
      case JBool(_)               => GoBool
      case JArray(arr) =>
        if (arr.isEmpty) GoArray(GoAny)
        else GoArray(GoType(name, arr.head))
      case JObject(_) => GoStruct(name)
      case _          => GoAny
    }

}
