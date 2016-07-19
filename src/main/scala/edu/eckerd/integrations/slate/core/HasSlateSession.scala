package edu.eckerd.integrations.slate.core

import akka.http.scaladsl.model.headers.BasicHttpCredentials

/**
  * Created by davenpcm on 7/19/16.
  */
trait HasSlateSession {
  val credentials: BasicHttpCredentials
  val link: String
}
