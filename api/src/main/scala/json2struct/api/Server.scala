package json2struct.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import json2struct.Conf.APP_CONF
import json2struct.Converter
import json2struct.Printer.Syntax.toPrinterOps
import json2struct.api.Conf.HttpPort
import json2struct.api.JsonSupport.Json

object Server extends Directives with JsonSupport {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("server", APP_CONF)

    val route = concat(
      pathPrefix("v1" / "convert") {
        path("json") {
          post {
            entity(as[Json]) { json =>
              complete {
                Converter.convertJson(json.json, json.name)
                  .map(_.print())
                  .mkString(System.lineSeparator())
              }
            }
          }
        } ~
          path("struct") {
            post {
              entity(as[String]) { struct =>
                complete {
                  Converter.convertStruct(struct).map(_.print()).mkString(System.lineSeparator())
                }
              }
            }
          }
      },
      pathPrefix("v2" / "convert") {
        path("json") {
          post {
            parameters("name") { name =>
              entity(as[String]) { json =>
                complete {
                  Converter.convertJson(json, name)
                    .map(_.print())
                    .mkString(System.lineSeparator())
                }
              }
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
    println(s"Server started on 0.0.0.0:$HttpPort")
  }
}
