package json2struct


import scala.collection.mutable


case class Struct(name: String, fields: Map[String, GoType]) {
  override def toString: String = {
    s"struct $name" + s", fields: $fields"
  }
}


trait Show[T] {
  def show(t: T): String
}

object Struct {

  type StructField = (String, GoType)

  def apply(name: String, fields: Seq[StructField]): Struct = {
    val builder = mutable.Map.newBuilder[String, GoType]
    fields.foldLeft(builder) { case (m, p) => builder += p }
    Struct(name, builder.result().toMap)
  }

  object ShowStruct extends Show[Struct] {
    override def show(s: Struct): String = {
      s.toString
    }
  }

}
