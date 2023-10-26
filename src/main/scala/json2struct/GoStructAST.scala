package json2struct

object GoStructAST {
  sealed trait Field {

    def name: String

    def tpe: GoType

    def tag: Tag

  }

  object Field {

    def unapply(f: Field): Option[(String, GoType, Tag)] = Some(f.name, f.tpe, f.tag)

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
    case class Simple(props: Map[String, Seq[String]]) extends Tag

    case object None extends Tag
  }

  case class Struct(name: String, fields: Seq[Field])

}
