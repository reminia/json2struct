package json2struct

import json2struct.GoType.GoArray

object GoStructAST {
  sealed trait Field {

    def name: String

    def tpe: GoType

    def tag: Tag

    def isArray: Boolean = tpe match {
      case _: GoArray => true
      case _ => false
    }

    def isStruct: Boolean = this match {
      case _: Struct => true
      case _ => false
    }
  }

  object Field {

    def unapply(f: Field): Option[(String, GoType, Tag)] = Some(f.name, f.tpe, f.tag)

    case class Simple(name: String, tpe: GoType, tag: Tag = Tag.None)
      extends Field
  }


  sealed trait Tag

  object Tag {
    case class Simple(props: Map[String, Seq[String]]) extends Tag

    case object None extends Tag
  }

  case class Struct(name: String, fields: Seq[Field], tag: Tag = Tag.None) extends Field {
    def this(name: String) = {
      this(name, Seq.empty)
    }

    override def tpe: GoType = GoType.GoStruct(name)
  }
}
