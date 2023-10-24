package json2struct


import json2struct.GoType.{GoBool, GoInt, GoString, upper}

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

      def tag(field: (String, GoType)): String =
        blank(4) + s"""`json:"${field._1}"`"""

      val fields = s.fields.map {
        case tpl@(name, tpe) => s"${upper(name)} ${tpe.desc} ${tag(tpl)}\n"
      }

      val sb = new StringBuilder
      sb.append(s"type ${upper(s.name)} struct {\n")
      fields.foreach(f => sb.append(blank(4)).append(f))
      sb.append("}")
      sb.toString()
    }
  }

  def blank(n: Int): String = " " * n

  def main(args: Array[String]): Unit = {
    val struct = Struct(
      "data",
      Map("code" -> GoInt, "msg" -> GoString, "Success" -> GoBool))
    println(ShowStruct.show(struct))
  }
}
