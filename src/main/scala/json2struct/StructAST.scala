package json2struct

object StructAST {
  sealed trait Field {

    def name: String

    def tpe: GoType

    def tag: Tag

    def jsonName: String = tag match {
      case Tag.Simple(_name) => _name
      case _ => name
    }

  }

  object Field {
    case class Simple(name: String, tpe: GoType, tag: Tag = Tag.None)
      extends Field

    case class Array(name: String, tpe: GoType, tag: Tag = Tag.None)
      extends Field

    case class Struct(name: String, tag: Tag = Tag.None) extends Field {
      override def tpe: GoType = GoType.GoStruct(name)
    }
  }


  sealed trait Tag

  object Tag {
    case class Simple(name: String) extends Tag

    case object None extends Tag
  }

  case class Struct(name: String, fields: Seq[Field])

}
