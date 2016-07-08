name := "slate-core"
organization := "edu.eckerd"
version := "0.0.1.0-SNAPSHOT"
scalaVersion := "2.11.8"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka" %% "akka-http-core" % "2.4.7",
    "com.typesafe.akka" %% "akka-http-testkit" % "2.4.7" % "test",
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.7"
  )
}