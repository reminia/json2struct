package json2struct

class StructAST {
  sealed trait Field {
    def name: String

    def tpe: GoType

    def tag: Tag

    def jsonName: String = tag match {
      case SimpleTag(_name) => _name
      case _ => name
    }
  }

  case class SimpleField(name: String, tpe: GoType, tag: Tag)
    extends Field

  case class ArrayField(name: String, tpe: GoType, tag: Tag)
    extends Field

  case class StructField(name: String, tag: Tag) extends Field {
    override def tpe: GoType = GoType.GoStruct(name)
  }

  sealed trait Tag

  case class SimpleTag(name: String) extends Tag

  case object None extends Tag

  case class Struct(name: String, fields: Seq[Field])

}
