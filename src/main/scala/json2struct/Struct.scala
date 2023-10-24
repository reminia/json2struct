package json2struct

case class Struct(name: String, fields: Map[String, GoType]) {
  override def toString: String = {
    s"struct $name" + s", fields: $fields"
  }
}

sealed trait GoType {
  def desc: String

  override def toString: String = desc
}

object GoInt extends GoType {
  override def desc = "int"
}

object GoString extends GoType {
  override def desc: String = "string"
}

object GoBoolean extends GoType {
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

trait Show[T] {
  def show(t: T): String
}

object Struct {

  object ShowStruct extends Show[Struct] {
    override def show(s: Struct): String = {
      s.toString
    }
  }

  def main(args: Array[String]): Unit = {
    println(GoInt.toString)
    println(GoArray(GoInt))
    println(Struct("Data", Map("msg" -> GoString, "length" -> GoInt)))
  }

}
