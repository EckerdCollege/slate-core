# Slate Core [![Build Status](https://travis-ci.org/ChristopherDavenport/slate-core.svg?branch=master)](https://travis-ci.org/ChristopherDavenport/slate-core) [![codecov](https://codecov.io/gh/ChristopherDavenport/slate-core/branch/master/graph/badge.svg)](https://codecov.io/gh/ChristopherDavenport/slate-core) [![Stories in Ready](https://badge.waffle.io/ChristopherDavenport/slate-core.png?label=ready&title=Ready)](https://waffle.io/ChristopherDavenport/slate-core)


This is a base library for utilizing the ability to pull and parse
default responses from Slate. Currently this is used to parse their
default response format so that the developer can transition immediately
to consuming and working with the data they are trying to work with, 
rather than working on interacting with the Slate Json.

Those who utilize the library are going to need to extend the 
DefaultJsonProtocol with their custom json class and then they can place a 
Request for the object.

To make a request simply create a request and then retrieve it, or utilize the
SingleRequest feature on the accompanying object. 

Please be aware when looking at the code examples that currently akka http 
is echoing an annoying but useless println which may show up mid-code 
due to its asynchronous nature.

To Utilize From SBT
```sbt
resolvers += Resolver.sonatypeRepo("snapshots")
libraryDependencies := "edu.eckerd" %% "slate-core" % "0.1.0-SNAPSHOT"
```

First You Must Have A Class and a Json Representation for the class
```scala
scala> import edu.eckerd.integrations.slate.core.DefaultJsonProtocol
import edu.eckerd.integrations.slate.core.DefaultJsonProtocol

scala> case class NameID(name: String, id: String)
defined class NameID

scala> object myProtocol extends DefaultJsonProtocol {
     |   implicit val NameIDFormat = jsonFormat2(NameID)
     | }
defined object myProtocol
```
If you want to make a SingleRequest Use The Single Request Feature. You will
need to make sure your jsonProtocols are in scope to Unmarshall the response.
You can write blocking code if you want to, but I don't recommend it.
```scala
scala> import edu.eckerd.integrations.slate.core.Request
import edu.eckerd.integrations.slate.core.Request

scala> import myProtocol._
import myProtocol._

scala> import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext.Implicits.global

scala> import scala.concurrent.Await
import scala.concurrent.Await

scala> import scala.concurrent.duration._
import scala.concurrent.duration._

scala> val futureResponse = Request.SingleRequest[NameID]("user", "password", ""https://www.testendpoint.com")
futureResponse: scala.concurrent.Future[Seq[NameID]] = List()

scala> val response = Await.result(futureResponse, 1.second)
sslConfig.config.loose.disableSNI = false
response: Seq[NameID] = List(NameID(ExampleName,ExampleID))
```
For Repeated Uses Utilize Your own actor system and actor materializer.
```scala
scala> import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext.Implicits.global

scala> import scala.util.{Success, Failure}
import scala.util.{Success, Failure}

scala> import edu.eckerd.integrations.slate.core.Request
import edu.eckerd.integrations.slate.core.Request

scala> import myProtocol._
import myProtocol._

scala> import akka.actor.ActorSystem
import akka.actor.ActorSystem

scala> import akka.stream.ActorMaterializer
import akka.stream.ActorMaterializer

scala> implicit val system = ActorSystem("READMEsystem")
system: akka.actor.ActorSystem = akka://READMEsystem

scala> implicit val materializer = ActorMaterializer()
materializer: akka.stream.ActorMaterializer = ActorMaterializerImpl(akka://READMEsystem,ActorMaterializerSettings(4,16,,<function1>,StreamSubscriptionTimeoutSettings(CancelTermination,5000 milliseconds),false,1000,1000,false,true),akka.dispatch.Dispatchers@503b5258,Actor[akka://READMEsystem/user/StreamSupervisor-3#-940597389],false,akka.stream.impl.SeqActorNameImpl@1ae4546b)

scala> val request = Request[NameID]("user", "password", "https://www.testendpoint.com")
request: edu.eckerd.integrations.slate.core.Request[NameID] = edu.eckerd.integrations.slate.core.Request@6588186d

scala> val r1 = request.retrieve
sslConfig.config.loose.disableSNI = false
r1: scala.concurrent.Future[Seq[NameID]] = List()

scala> val r2 = request.retrieve
sslConfig.config.loose.disableSNI = false
r2: scala.concurrent.Future[Seq[NameID]] = List()

scala> val response1 = r1 onComplete {
     |     case Success(response) => println(s"Response from r1: ${response}")
     |     case Failure(e) => println(s"An Error Occured in r1: ${e.getMessage}")
     | }
Response from r1: List(NameID(ExampleName,ExampleID))
response1: Unit = ()

scala> val response2 = r2 onComplete {
     |     case Success(response) => println(s"Response from r2: ${response}")
     |     case Failure(e) => println(s"An Error Occured in r2: ${e.getMessage}")
     | }
Response from r2: List(NameID(ExampleName,ExampleID))
response2: Unit = ()

scala> val term = system.terminate()
term: scala.concurrent.Future[akka.actor.Terminated] = List()
```



