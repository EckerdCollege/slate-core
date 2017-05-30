lazy val buildSettings = Seq(
  organization := "edu.eckerd",
  version := "0.1.2-SNAPSHOT",
  scalaVersion := "2.12.2",
  crossScalaVersions := Seq("2.11.11", scalaVersion.value)
)

lazy val dependencySettings = Seq(
  libraryDependencies ++= {
    val akkaHttpV = "10.0.7"
    Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpV,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % "test"
    )
  }
)

lazy val scoverageSettings = Seq(
  coverageMinimum := 60,
  coverageFailOnMinimum := false,
  coverageExcludedFiles := ".*/src/test/.*"
)

lazy val commonSettings = Seq(
  scalacOptions := Seq(
    "-feature",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-Xfatal-warnings",
    "-deprecation",
    "-unchecked"
  ),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/"
  ),
  scalacOptions in console in Compile -= "-Xfatal-warnings",
  scalacOptions in console in Test -= "-Xfatal-warnings"
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("Snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("Releases" at nexus + "service/local/staging/deploy/maven2")
  },
  homepage := Some(url("https://github.com/EckerdCollege/slate-core")),
  licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  scmInfo := Some(ScmInfo(
    url("https://github.com/EckerdCollege/slate-core"),
    "scm:git@github.com:EckerdCollege/slate-core.git")),
  pomExtra := (
    <developers>
      <developer>
        <id>ChristopherDavenport</id>
        <name>Christopher Davenport</name>
        <url>https://github.com/ChristopherDavenport</url>
      </developer>
    </developers>
    )
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val coreSettings = buildSettings ++ commonSettings ++ publishSettings ++
  scoverageSettings ++ dependencySettings


lazy val root = project.in(file("."))
  .settings(moduleName := "slate-core")
  .settings(coreSettings:_*)
  .settings(publishSettings)

credentials in ThisBuild ++= (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq