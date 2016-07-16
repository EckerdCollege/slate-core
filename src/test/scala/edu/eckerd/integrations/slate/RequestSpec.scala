package edu.eckerd.integrations.slate
import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import edu.eckerd.integrations.slate.core.DefaultJsonProtocol
import edu.eckerd.integrations.slate.core.RequestTrait
import edu.eckerd.integrations.slate.core.model.SlateRequest
import edu.eckerd.integrations.slate.core.model.SlateResponse
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalamock.scalatest.MockFactory
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.Future
/**
  * Created by davenpcm on 7/7/16.
  */
class RequestSpec extends TestKit(ActorSystem("RequestSpec"))
with WordSpecLike with Matchers with MockFactory with BeforeAndAfterAll {

  class MockRequest[A](reqRespPairs:Seq[(SlateRequest, String)])
                      (implicit val um: Unmarshaller[ResponseEntity, SlateResponse[A]]) extends RequestTrait[A]{

    override implicit val actorSystem = system

    override implicit val ec = actorSystem.dispatcher

    override implicit val actorMaterializer = ActorMaterializer()(system)

    val mock = mockFunction[HttpRequest, Future[HttpResponse]]

    override val responder: HttpResponder = mock

    reqRespPairs.foreach{
      case (slateRequest, responseString) =>

        val req = HttpRequest(
          HttpMethods.GET,
          slateRequest.link,
          headers = List(
            Authorization(
              BasicHttpCredentials(slateRequest.user, slateRequest.password)
            )
          )
        )
        val resp = HttpResponse(
          status = StatusCodes.OK,
          entity = HttpEntity(
            ContentType(
              MediaTypes.`application/json`
            ),
            responseString
          )
        )
        mock.expects(req).returning(Future.successful(resp))
    }

  }

  "Request" should {

    "Marshall Responses to Strings" in {
      val myProtocol = new DefaultJsonProtocol {}
      import myProtocol._

      val expectedOutcome = List("yellow")

      val json = s"""{"row" : ["yellow"]}"""
      val request = SlateRequest("link", "user", "password")
      val mock = new MockRequest[String](Seq((request, json)))
      Await.result(mock.retrieve(request), 1.second) should be (expectedOutcome)
    }

    "Marshall Responses to Custom Classes" in {
      case class NameID(name: String, id: String)
      object myProtocol extends DefaultJsonProtocol {
        implicit val NameIDFormat = jsonFormat2(NameID)
      }
      import myProtocol._
      val expectedOutcome = List(NameID("Chris", "1528745"), NameID("Frank", "1259584"))
      val json ="""{"row":[{"name":"Chris", "id":"1528745"},{"name":"Frank","id":"1259584"}]}"""
      val request = SlateRequest("link", "user", "password")
      val mock = new MockRequest[NameID](Seq((request, json)))
      Await.result(mock.retrieve(request), 1.second) should be (expectedOutcome)
    }

  }

  "Companion Object" should {
    "be able to parse a configuration" in {
      val r = edu.eckerd.integrations.slate.core.Request.forConfig("slate")
      r should be (SlateRequest("www.test.com", "testUser", "testPassword"))
    }
  }

  override def afterAll(): Unit = {
    Await.ready(system.terminate(), Duration.Inf)
  }

}
