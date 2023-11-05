package json2struct.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import json2struct.api.JsonSupport._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val jsonFormat: RootJsonFormat[Json] = jsonFormat2(Json.apply)
  implicit val structFormat: RootJsonFormat[Struct] = jsonFormat1(Struct.apply)
}

object JsonSupport {
  final case class Json(name: String, json: String)

  final case class Struct(struct: String)
}