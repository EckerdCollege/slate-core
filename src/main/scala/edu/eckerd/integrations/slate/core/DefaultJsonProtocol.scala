package edu.eckerd.integrations.slate.core

import akka.http.scaladsl.marshallers.sprayjson.{SprayJsonSupport => AkkaSprayJsonSupport}
import edu.eckerd.integrations.slate.core.model.SlateResponse
import spray.json.{DefaultJsonProtocol => SprayDefaultJsonProtocol}
import spray.json.JsonFormat
/**
  * Created by davenpcm on 7/7/16.
  */
trait DefaultJsonProtocol extends AkkaSprayJsonSupport with SprayDefaultJsonProtocol {
  implicit def SlateResponseFormat[A: JsonFormat] = jsonFormat1(SlateResponse.apply[A])
}
