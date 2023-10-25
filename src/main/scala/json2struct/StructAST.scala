package json2struct

object StructAST {
  sealed trait Field {

    private var _tag: Tag = Tag.None

    def name: String

    def tpe: GoType

    def tag: Tag = _tag

    def jsonName: String = tag match {
      case Tag.Simple(_name) => _name
      case _ => name
    }

    def setTag(t: Tag): Field = {
      _tag = tag
      this
    }
  }

  object Field {
    case class Simple(name: String, tpe: GoType)
      extends Field

    case class Array(name: String, tpe: GoType)
      extends Field

    case class Struct(name: String) extends Field {
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
