package json2struct.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import com.typesafe.config.ConfigFactory
import json2struct.Conf.APP_CONF
import json2struct.Converter
import json2struct.Printer.Syntax.toPrinterOps
import json2struct.api.Conf.HttpPort
import json2struct.api.JsonSupport.*
import spray.json.*

import scala.concurrent.ExecutionContextExecutor
import scala.jdk.CollectionConverters.MapHasAsJava
import scala.util.{Failure, Success}

object Server extends Directives with JsonSupport {

  implicit val system: ActorSystem = ActorSystem("server", APP_CONF)
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val route = concat(
    pathPrefix("v1" / "convert") {
      path("json") {
        post {
          entity(as[JsonBody]) { body =>
            complete {
              Converter.convertJson(body.json, body.name)
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
          parameters(Symbol("name").?) { name =>
            entity(as[String]) { json =>
              complete {
                Converter.convertJson(json, name.getOrElse("Root"))
                  .map(_.print())
                  .mkString(System.lineSeparator())
              }
            }
          }
        }
      } ~
        path("struct") {
          post {
            optionalHeaderValueByName("config") { config =>
              entity(as[String]) { struct =>
                val conf = config.fold[Map[String, Any]](Map.empty) { conf =>
                  conf.parseJson.convertTo[Map[String, Any]]
                }.asJava
                complete {
                  Converter.convertStruct(
                      struct,
                      ConfigFactory.parseMap(conf).withFallback(APP_CONF)
                    )
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

  def main(args: Array[String]): Unit = {
    Http().newServerAt("0.0.0.0", HttpPort)
      .bind(route)
      .onComplete {
        case Success(_) =>
          println(s"Server started on 0.0.0.0:$HttpPort")
        case Failure(e) =>
          println(s"Server started failed on 0.0.0.0:$HttpPort due to " + e.getMessage)
      }
  }

}
