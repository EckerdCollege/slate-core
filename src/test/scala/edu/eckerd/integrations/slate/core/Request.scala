package edu.eckerd.integrations.slate.core

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import edu.eckerd.integrations.slate.core.model.SlateRequest
import edu.eckerd.integrations.slate.core.model.SlateResponse
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Authorization
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.stream.ActorMaterializer
import akka.stream.ActorMaterializerSettings
/**
  * Created by davenpcm on 7/7/16.
  */

object Request {

  def forConfig(configLocation: String): SlateRequest = {
    val config = ConfigFactory.load()
    val slateConfig = config.getConfig(configLocation)
    val user = slateConfig.getString("user")
    val password = slateConfig.getString("password")
    val link = slateConfig.getString("link")
    SlateRequest(link, user, password)
  }

  def TransformData[A](slateRequest: SlateRequest)(
    implicit ec: ExecutionContext,
    um: Unmarshaller[ResponseEntity, SlateResponse[A]]
  ): Future[Seq[A]] = {

    implicit val system = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))
    val authorization = Authorization(BasicHttpCredentials(slateRequest.user, slateRequest.password))

    Http(system)
      .singleRequest(
        HttpRequest(
          uri = slateRequest.link,
          headers = List(authorization)
        )
      )
      .flatMap {
      case HttpResponse(StatusCodes.OK, headers, entity, _) =>
        for {
          slateResponse <- Unmarshal(entity).to[SlateResponse[A]]
          shutdownHttp <- Http(system).shutdownAllConnectionPools()
          terminate <- system.terminate()
        } yield slateResponse.row
      case HttpResponse(code, _, _, _) =>
        for {
          shutdownHttp <- Http(system).shutdownAllConnectionPools()
          terminate <- system.terminate()
          failure <- Future.failed(new Throwable(s"Received invalid response code - $code"))
        } yield failure
    }
  }

}
