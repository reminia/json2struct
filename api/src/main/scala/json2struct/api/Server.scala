package json2struct.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import json2struct.api.Conf.{HttpPort, APP_CONF}

object Server {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("server", APP_CONF)
    implicit val executionContext = system.dispatcher

    val route = concat(
      path("v1") {
        path("json") {
          post {
            complete(StatusCodes.OK, "healthy!")
          }
        }
      },
      path("health") {
        get {
          complete(StatusCodes.OK, "healthy!")
        }
      }
    )

    Http().newServerAt("0.0.0.0", HttpPort).bind(route)
  }
}
