package json2struct

import org.json4s.JsonAST.{JInt, JObject, JString, JValue}
import org.json4s.{JArray, JBool}

sealed trait GoType {
  def desc: String

  override def toString: String = desc
}

object GoType {
  object GoInt extends GoType {
    override def desc = "int"
  }

  object GoFloat extends GoType {
    override def desc: String = "float32"
  }

  object GoString extends GoType {
    override def desc: String = "string"
  }

  object GoBool extends GoType {
    override def desc: String = "bool"
  }

  case class GoArray(element: GoType) extends GoType {
    override def desc: String = s"[]${element}"
  }

  case class GoStruct(name: String) extends GoType {
    assert(name.nonEmpty)

    override def desc: String = {
      name.toCharArray.toList match {
        case fst :: tail if fst.isLower =>
          (fst.toUpper :: tail).mkString
        case _ => name
      }
    }
  }

  def from(value: JValue): GoType = {
    value match {
      case JInt(_) => GoInt
      case JString(_) => GoString
      case JBool(_) => GoBool
      //todo: name is empty
      case JObject(_) => GoStruct("")
      // todo: arr can be empty, add an unknown go type
      case JArray(arr) => GoArray(from(arr(0)))
      case _ => throw new IllegalArgumentException("not support json type " + value.toString)
    }
  }
}
