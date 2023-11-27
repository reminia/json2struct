package json2struct.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import json2struct.api.JsonSupport.*
import spray.json.*

import scala.language.implicitConversions

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val anyMapFormat: JsonFormat[Map[String, Any]] =
    mapFormat[String, Any](StringJsonFormat, AnyFormat)
  implicit val jsonFormat: RootJsonFormat[JsonBody] = jsonFormat2(JsonBody.apply)
}

object JsonSupport {
  final case class JsonBody(name: String, json: String)

  implicit def toMapObject(m: Map[String, Any]): Map[String, Object] =
    m.map {
      case (k, v) => k -> v.asInstanceOf[Object]
    }

  implicit object AnyFormat extends JsonFormat[Any] {
    override def write(any: Any): JsValue = any match {
      case b: Boolean => JsBoolean(b)
      case i: Int => JsNumber(i)
      case s: String => JsString(s)
      case _ =>
        throw new SerializationException(s"cannot serialize $any, type not support")
    }

    override def read(json: JsValue): Any = json match {
      case JsBoolean(b) => b
      case JsString(s) => s
      case JsNumber(n) => n
      case _ => throw DeserializationException(s"cannot deserialize $json, type not support")
    }
  }

}