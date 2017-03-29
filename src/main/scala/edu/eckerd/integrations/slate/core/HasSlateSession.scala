package edu.eckerd.integrations.slate.core

import akka.http.scaladsl.model.headers.BasicHttpCredentials

trait HasSlateSession {
  val credentials: BasicHttpCredentials
  val link: String
}
