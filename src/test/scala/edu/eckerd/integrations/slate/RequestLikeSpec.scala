package edu.eckerd.integrations.slate

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import edu.eckerd.integrations.slate.core.{DefaultJsonProtocol, RequestLike}
import edu.eckerd.integrations.slate.core.model.SlateResponse
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by davenpcm on 7/26/16. This file is for parsing Business Logic, where request is the piece that actually
  * goes out with an Http session and does something so test regarding parsing logic should be placed here.
  */
class RequestLikeSpec extends TestKit(ActorSystem("RequestLikeSpec"))
  with FlatSpecLike with Matchers with MockFactory with BeforeAndAfterAll {

  case class SlateRequest(user: String, password: String, link: String)

  case class TestResponse(jsonResponse: String, statusCode: StatusCode)

  class MockRequest[A](request: SlateRequest, testResponse: TestResponse)
                      (implicit val um: Unmarshaller[ResponseEntity, SlateResponse[A]]) extends RequestLike[A]{

    override implicit val actorSystem = system
    override implicit val ec = actorSystem.dispatcher
    override implicit val actorMaterializer = ActorMaterializer()(system)

    val user = request.user
    val password = request.password
    val credentials = BasicHttpCredentials(user, password)

    val link = request.link
    val mock = mockFunction[HttpRequest, Future[HttpResponse]]

    override val responder: HttpResponder = mock

    val req = HttpRequest(
      HttpMethods.GET,
      link,
      headers = List(
        Authorization(
          credentials
        )
      )
    )
    val resp = HttpResponse(
      testResponse.statusCode,
      entity = HttpEntity(
        ContentType(
          MediaTypes.`application/json`
        ),
        testResponse.jsonResponse
      )
    )
    mock.expects(req).returning(Future.successful(resp))

  }

  "Retrieve" should "Marshall Responses to Strings" in {
    val myProtocol = new DefaultJsonProtocol {}
    import myProtocol._

    val expectedOutcome = List("yellow")

    val json = s"""{"row" : ["yellow"]}"""
    val request = SlateRequest("link", "user", "password")
    val response = TestResponse(json, StatusCodes.OK)
    val mock = new MockRequest[String](request, response)
    Await.result(mock.retrieve(), 1.second) should be (expectedOutcome)
  }

  it should "Marshall Responses to Custom Classes" in {
    case class NameID(name: String, id: String)
    object myProtocol extends DefaultJsonProtocol {
      implicit val NameIDFormat = jsonFormat2(NameID)
    }
    import myProtocol._
    val expectedOutcome = List(NameID("Chris", "1528745"), NameID("Frank", "1259584"))
    val json ="""{"row":[{"name":"Chris", "id":"1528745"},{"name":"Frank","id":"1259584"}]}"""
    val request = SlateRequest("link", "user", "password")
    val response = TestResponse(json, StatusCodes.OK)
    val mock = new MockRequest[NameID](request, response)
    Await.result(mock.retrieve(), 1.second) should be (expectedOutcome)
  }

  it should "Respond with an Empty Sequence on Error Code 500" in {
    case class NameID(name: String, id: String)
    object myProtocol extends DefaultJsonProtocol {
      implicit val NameIDFormat = jsonFormat2(NameID)
    }
    import myProtocol._
    val expectedOutcome = List[NameID]()
    val json =""""""
    val request = SlateRequest("link", "user", "password")
    val response = TestResponse(json, StatusCodes.InternalServerError)
    val mock = new MockRequest[NameID](request, response)
    Await.result(mock.retrieve(), 1.second) should be (expectedOutcome)
  }

  it should "Throw An Error On Another Status Code" in {
    case class NameID(name: String, id: String)
    object myProtocol extends DefaultJsonProtocol {
      implicit val NameIDFormat = jsonFormat2(NameID)
    }
    import myProtocol._
    val json =""""""
    val request = SlateRequest("link", "user", "password")
    val response = TestResponse(json, StatusCodes.BadRequest)
    val mock = new MockRequest[NameID](request, response)
    intercept[Throwable]{
      Await.result(mock.retrieve(), 1.second)
    }
  }

  override def afterAll(): Unit = {
    Await.ready(system.terminate(), Duration.Inf)
  }

}
