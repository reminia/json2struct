package json2struct

import org.json4s.JsonAST._
import org.json4s.{JArray, JBool}

sealed trait GoType {
  def desc: String

  override def toString: String = desc
}

object GoType {
  case object GoInt extends GoType {
    override def desc = "int"
  }

  case object GoFloat extends GoType {
    override def desc: String = "float32"
  }

  case object GoString extends GoType {
    override def desc: String = "string"
  }

  case object GoBool extends GoType {
    override def desc: String = "bool"
  }

  case class GoArray(element: GoType) extends GoType {
    override def desc: String = s"[]$element"
  }

  case class GoStruct(name: String) extends GoType {
    override def desc: String = {
      if (name.isEmpty)
        "Unknown"
      else upper(name)
    }
  }

  case object Unknown extends GoType {
    override def desc: String = "Unknown"
  }

  def apply(value: JValue, name: String): GoType = {
    value match {
      case JInt(_) => GoInt
      case JDouble(_) => GoFloat
      case JString(_) => GoString
      case JBool(_) => GoBool
      case JArray(arr) =>
        if (arr.isEmpty) GoArray(Unknown)
        else GoArray(GoType(arr.head, name))
      case JObject(_) => GoStruct(name)
      case _ => Unknown
    }
  }

  // uppercase first char
  def upper(s: String): String = {
    s.toCharArray.toList match {
      case fst :: tail if fst.isLower =>
        (fst.toUpper :: tail).mkString
      case _ => s
    }
  }

}
