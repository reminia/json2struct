import akka.http.scaladsl.model.*
import akka.util.ByteString
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import json2struct.api.Server.*

import scala.concurrent.duration.*
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters.*

/** handler for aws lambda */
object LambdaHandler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {

  override def handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    context.getLogger.log("request event:")
    context.getLogger.log(input.toString)
    val query = input.getQueryStringParameters.asScala.map {
      case (k, v) => s"$k=$v"
    }.mkString("?", "&", "")
    context.getLogger.log("uri query:" + query)
    val request = HttpRequest(
      method = HttpMethods.getForKey(input.getHttpMethod).getOrElse(HttpMethods.POST),
      uri = Uri(input.getPath + query),
      entity = HttpEntity(ContentTypes.`application/json`, input.getBody)
    )
    val responseFuture: Future[HttpResponse] = route(request)
    val response: HttpResponse = Await.result(responseFuture, 10.seconds)

    val responseBodyFuture: Future[String] = response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String)
    val responseBody: String = Await.result(responseBodyFuture, 10.seconds)

    new APIGatewayProxyResponseEvent()
      .withStatusCode(response.status.intValue)
      .withHeaders(Map("Content-Type" -> "application/json").asJava)
      .withBody(responseBody)
  }
}
