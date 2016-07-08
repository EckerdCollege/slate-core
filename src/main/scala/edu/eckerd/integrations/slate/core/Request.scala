package edu.eckerd.integrations.slate.core

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, ResponseEntity, StatusCodes}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.config.ConfigFactory
import edu.eckerd.integrations.slate.core.model.SlateRequest
import edu.eckerd.integrations.slate.core.model.SlateResponse
import scala.concurrent.{ExecutionContext, Future}
/**
  * Created by davenpcm on 7/7/16.
  */

trait RequestTrait[A]{
  type HttpResponder = HttpRequest => Future[HttpResponse]
  def responder: HttpResponder
  implicit val actorSystem: ActorSystem
  implicit val actorMaterializer: ActorMaterializer
  implicit val ec: ExecutionContext
  implicit val um: Unmarshaller[ResponseEntity, SlateResponse[A]]

  def retrieve(slateRequest: SlateRequest): Future[Seq[A]] = {
    val authorization = Authorization(BasicHttpCredentials(slateRequest.user, slateRequest.password))
    responder(
      HttpRequest(
        uri = slateRequest.link,
        headers = List(authorization)
      )
    ).flatMap {
      case HttpResponse(StatusCodes.OK, headers, entity, _) =>
        for {
          slateResponse <- Unmarshal(entity).to[SlateResponse[A]]
        } yield slateResponse.row
      case HttpResponse(code, _, _, _) =>
        for {
          failure <- Future.failed(new Throwable(s"Received invalid response code - $code"))
        } yield failure
    }
  }

}

class Request[A](slateRequest: SlateRequest)(
                  implicit val actorSystem: ActorSystem,
                  val actorMaterializer: ActorMaterializer,
                  val um: Unmarshaller[ResponseEntity, SlateResponse[A]]
                )
  extends RequestTrait[A] {

  def retrieve(): Future[Seq[A]] = super.retrieve(slateRequest)
  override val ec: ExecutionContext = actorSystem.dispatcher
  override def responder = Http().singleRequest(_)

}

object Request {

  def forConfig(configLocation: String): SlateRequest = {
    val config = ConfigFactory.load()
    val slateConfig = config.getConfig(configLocation)
    val user = slateConfig.getString("user")
    val password = slateConfig.getString("password")
    val link = slateConfig.getString("link")
    SlateRequest(link, user, password)
  }

  def SingleRequest[A](slateRequest: SlateRequest)(
    implicit ec: ExecutionContext,
    um: Unmarshaller[ResponseEntity, SlateResponse[A]]
  ): Future[Seq[A]] = {

    implicit val system = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))
//    val authorization = Authorization(BasicHttpCredentials(slateRequest.user, slateRequest.password))

    val returnValue = new Request(slateRequest).retrieve()

    for {
      finalValue <- returnValue
      terminate <- system.terminate()
    } yield finalValue

//    Http(system)
//      .singleRequest(
//        HttpRequest(
//          uri = slateRequest.link,
//          headers = List(authorization)
//        )
//      )
//      .flatMap {
//      case HttpResponse(StatusCodes.OK, headers, entity, _) =>
//        for {
//          slateResponse <- Unmarshal(entity).to[SlateResponse[A]]
//          shutdownHttp <- Http(system).shutdownAllConnectionPools()
//          terminate <- system.terminate()
//        } yield slateResponse.row
//      case HttpResponse(code, _, _, _) =>
//        for {
//          shutdownHttp <- Http(system).shutdownAllConnectionPools()
//          terminate <- system.terminate()
//          failure <- Future.failed(new Throwable(s"Received invalid response code - $code"))
//        } yield failure
//    }
  }

}
