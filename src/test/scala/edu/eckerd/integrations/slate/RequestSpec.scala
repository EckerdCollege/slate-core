package edu.eckerd.integrations.slate
import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.unmarshalling.Unmarshaller.UnsupportedContentTypeException
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import edu.eckerd.integrations.slate.core.{DefaultJsonProtocol, Request}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import org.scalamock.scalatest.MockFactory

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class RequestSpec extends TestKit(ActorSystem("RequestSpec"))
  with FlatSpecLike with Matchers with MockFactory with BeforeAndAfterAll {

  "Request" should "should be able to make a request" in {
    case class NameID(name: String, id: String)
    object myProtocol extends DefaultJsonProtocol {
      implicit val NameIDFormat = jsonFormat2(NameID)
    }
    import myProtocol._
    implicit val materializer = ActorMaterializer()

    val r = Request[NameID]("user", "password", "link")

    intercept[IllegalUriException]{
      Await.result(r.retrieve(), 1.second)
    }
  }

  it should "should be unable to parse a request to a non-json endpoint" in {
    case class NameID(name: String, id: String)
    object myProtocol extends DefaultJsonProtocol {
      implicit val NameIDFormat = jsonFormat2(NameID)
    }
    import myProtocol._
    implicit val materializer = ActorMaterializer()

    val r = Request[NameID]("user", "password", "http://www.google.com")
    intercept[UnsupportedContentTypeException]{
      Await.result(r.retrieve(), 1.second)
    }
  }



  "Apply" should "be able to create a new Request" in {
    case class NameID(name: String, id: String)
    object myProtocol extends DefaultJsonProtocol {
      implicit val NameIDFormat = jsonFormat2(NameID)
    }
    import myProtocol._
    implicit val materializer = ActorMaterializer()

    val r = Request[NameID]("user", "password", "link")

    r.link should be ("link")
    r.credentials should be (BasicHttpCredentials("user", "password"))
  }

  "forConfig" should "be able to parse a configuration" in {
    case class NameID(name: String, id: String)
    object myProtocol extends DefaultJsonProtocol {
      implicit val NameIDFormat = jsonFormat2(NameID)
    }
    import myProtocol._
    implicit val materializer = ActorMaterializer()

    val r = Request.forConfig[NameID]("slate")

    r.link should be ("www.test.com")
    r.credentials should be (BasicHttpCredentials("testUser", "testPassword"))
  }

  "SingleRequest" should "should be able to make a request" in {
    case class NameID(name: String, id: String)
    object myProtocol extends DefaultJsonProtocol {
      implicit val NameIDFormat = jsonFormat2(NameID)
    }
    import myProtocol._
    val r = Request.SingleRequest[NameID]("user", "password", "http://www.google.com")
    intercept[UnsupportedContentTypeException]{
      Await.result(r, 1.second)
    }
  }

  "SingleRequestForConfig" should "fail to parse an invalid uri" in {
    case class NameID(name: String, id: String)
    object myProtocol extends DefaultJsonProtocol {
      implicit val NameIDFormat = jsonFormat2(NameID)
    }
    import myProtocol._
    implicit val materializer = ActorMaterializer()

    val r = Request.SingleRequestForConfig[NameID]("slate")
    intercept[IllegalUriException] {
      Await.result(r, 1.second)
    }
  }


  override def afterAll(): Unit = {
    Await.ready(system.terminate(), Duration.Inf)
  }

}
