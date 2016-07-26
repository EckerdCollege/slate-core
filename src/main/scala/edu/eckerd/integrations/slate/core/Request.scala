package edu.eckerd.integrations.slate.core

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, ResponseEntity, StatusCodes}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.config.ConfigFactory
import edu.eckerd.integrations.slate.core.model.SlateResponse
import scala.concurrent.{ExecutionContext, Future}
/**
  * Created by davenpcm on 7/7/16.
  */



class Request[A](val credentials: BasicHttpCredentials, val link: String)(
                  implicit val actorSystem: ActorSystem,
                  val actorMaterializer: ActorMaterializer,
                  val um: Unmarshaller[ResponseEntity, SlateResponse[A]]
                )
  extends RequestLike[A] {

  override val ec: ExecutionContext = actorSystem.dispatcher
  override def responder = Http().singleRequest(_)

}

object Request {

  /**
    * Generator for a request
    *
    * We generate the credentials in this step so that it will be encrypted for anyone who tries
    * to access the object
    *
    * @param user The user value that will be used to create authentication
    * @param password The password that will be used to create authentication
    * @param link The link to retrieve information from
    * @param um The unmarshaller from the Response to the final data return type
    * @param actorSystem The actor system to generate actors from
    * @param actorMaterializer The materializer to be able to generate actors on demand
    * @tparam A The type of the eventual set to return
    * @return A Request that can be used to return information from Slate
    */
  def apply[A](user: String, password: String, link: String)
              (implicit um : Unmarshaller[ResponseEntity, SlateResponse[A]],
               actorSystem: ActorSystem,
               actorMaterializer: ActorMaterializer
              ): Request[A] = {
    val credentials = BasicHttpCredentials(user, password)
    new Request[A](credentials, link)
  }

  private case class SlateRequestConfig(user: String, password: String, link: String)

  /**
    * This generates an internally understandable Configuration that can be parsed to create a Request
    * @param configLocation The location of the configuration object containing values user, passsword, link
    * @return A Slate Request Config
    */
  private def getSlateRequestConfig(configLocation: String): SlateRequestConfig = {
    val config = ConfigFactory.load()
    val slateConfig = config.getConfig(configLocation)
    val user = slateConfig.getString("user")
    val password = slateConfig.getString("password")
    val link = slateConfig.getString("link")

    SlateRequestConfig(user, password, link)
  }

  /**
    * The request for a configuration
    * @param configLocation The location in the config file to find the configuration
    * @param um The unmarshaller from the response to the type
    * @param actorSystem The actor system to generate actors from.
    * @param actorMaterializer The materializer to create actors.
    * @tparam A The type to return at the end.
    * @return A request that can return a Sequence of some type A
    */
  def forConfig[A](configLocation: String)
                  (implicit um : Unmarshaller[ResponseEntity, SlateResponse[A]],
                   actorSystem: ActorSystem,
                   actorMaterializer: ActorMaterializer
                  ): Request[A]  = {
    val slateRequestConfig = getSlateRequestConfig(configLocation)
    Request[A](slateRequestConfig.user, slateRequestConfig.password, slateRequestConfig.link)
  }

  /**
    * Takes a SingleRequest from the source. This generates and then kills its own actor system so this is a very
    * bulky operation that should only be used if you only need a single request
    * @param user The user to authorize with
    * @param password The password to authorize with
    * @param link The link to retrieve data from
    * @param ec The execution context
    * @param um The unmarshaller to the final type
    * @tparam A The final type
    * @return A Future Sequence of the type we would like to retrieve from slate
    */
  def SingleRequest[A](
                        user: String,
                        password: String,
                        link: String)
                      (
                        implicit ec: ExecutionContext,
                        um: Unmarshaller[ResponseEntity, SlateResponse[A]]
                      ): Future[Seq[A]] = {

    implicit val system = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))

    val returnValue = Request[A](user, password, link).retrieve()

    for {
      finalValue <- returnValue
      _ <- system.terminate()
    } yield finalValue
  }

  /**
    * This takes a config from a configuration file and then gets the result of that configuration
    * @param configLocation This is the location in the configuration file the required settings are found
    * @param ec The execution context
    * @param um The unmarshaller for the types
    * @tparam A The type to be return
    * @return A Future Sequence of the Type we would like
    */
  def SingleRequestForConfig[A](configLocation: String)
                               (implicit ec: ExecutionContext, um: Unmarshaller[ResponseEntity, SlateResponse[A]]
                               ): Future[Seq[A]] = {
    implicit val system = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))

    val slateRequestConfig = getSlateRequestConfig(configLocation)

    val returnValue = Request[A](
      slateRequestConfig.user,
      slateRequestConfig.password,
      slateRequestConfig.link
    ).retrieve()

    for {
      finalValue <- returnValue
      terminate <- system.terminate()
    } yield finalValue
  }
}
