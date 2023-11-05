package json2struct.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import json2struct.api.JsonSupport._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val jsonFormat: RootJsonFormat[Json] = jsonFormat2(Json.apply)
}

object JsonSupport {
  final case class Json(name: String, json: String)
}