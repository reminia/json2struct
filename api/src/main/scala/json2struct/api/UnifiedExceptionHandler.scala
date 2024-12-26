package json2struct.api

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, extractUri}
import akka.http.scaladsl.server.ExceptionHandler
import org.slf4j.LoggerFactory

object UnifiedExceptionHandler {

  private val logger = LoggerFactory.getLogger(this.getClass)

  implicit def default: ExceptionHandler =
    ExceptionHandler {
      case ex: Exception =>
        extractUri { uri =>
          logger.error(s"Request to $uri failed with: ${ex.getMessage}", ex)
          complete(HttpResponse(
            status = StatusCodes.InternalServerError,
            entity = HttpEntity(ex.getMessage)
          ))
        }
    }
}
