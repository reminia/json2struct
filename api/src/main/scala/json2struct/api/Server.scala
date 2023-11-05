package json2struct.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import json2struct.Converter
import json2struct.Printer.Syntax.toPrinterOps
import json2struct.api.Conf.{APP_CONF, HttpPort}
import json2struct.api.JsonSupport.{Json, Struct}

object Server extends Directives with JsonSupport {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("server", APP_CONF)

    val route = concat(
      path("v1/convert/json") {
        post {
          entity(as[Json]) { json =>
            complete {
              Converter.convertJson(json.json, json.name)
                .map(_.print())
                .mkString("\n")
            }
          }
        }
      },

      path("v1/convert/struct") {
        post {
          entity(as[Struct]) { struct =>
            complete {
              Converter.convertStruct(struct.struct).map(_.print())
            }
          }
        }
      },

      path("health") {
        get {
          complete(StatusCodes.OK, "I'm up!")
        }
      }
    )

    Http().newServerAt("0.0.0.0", HttpPort).bind(route)
  }
}
