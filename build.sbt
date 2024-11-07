import Dependencies.*

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.1"

val commonSettings = List(
  scalafmtOnCompile := false, // recommended in Scala 3
  testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
  libraryDependencies ++= List(
    CompilerPlugins.zerowaste,
    Libraries.cats.value,
    Libraries.catsEffect.value,
    Libraries.circeCore.value,
    Libraries.circeParser.value,
    Libraries.fs2Core.value,
    Libraries.kittens.value,
    Libraries.ip4sCore.value,
    Libraries.log4catsNoop,
    Libraries.monocleCore.value,
    Libraries.catsLaws         % Test,
    Libraries.monocleLaw       % Test,
    Libraries.scalacheck       % Test,
    Libraries.weaverCats       % Test,
    Libraries.weaverDiscipline % Test,
    Libraries.weaverScalaCheck % Test
  ) ++ Libraries.tapirCore ++ Libraries.tapirMetrics ++ Libraries.monixNewtypes
)

def dockerSettings(name: String) = List(
  Docker / packageName := s"password-$name",
  dockerBaseImage      := "jdk17-curl:latest",
  dockerExposedPorts ++= List(8080),
  makeBatScripts     := Nil,
  dockerUpdateLatest := true
)

lazy val domain = project
  .in(file("modules/domain"))
  .settings(commonSettings *)
  .settings(name := "domain")

lazy val core = project
  .in(file("modules/core"))
  .dependsOn(domain % "compile -> compile; test -> test")
  .settings(commonSettings *)
  .settings(name := "core")

lazy val server = project
  .in(file("modules/server"))
  .enablePlugins(DockerPlugin)
  .dependsOn(core)
  .settings(commonSettings *)
  .settings(dockerSettings("server"))
  .settings(libraryDependencies ++= Libraries.tapirServer)
  .settings(name := "server")

lazy val root = (project in file("."))
  .aggregate(domain, core, server)
  .settings(
    name := "password_manager"
  )
