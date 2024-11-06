import Dependencies._

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
  ) ++ Libraries.tapirCore ++ Libraries.tapirMetrics
)

def dockerSettings(name: String) = List(
  Docker / packageName := s"trading-$name",
  dockerBaseImage      := "jdk17-curl:latest",
  dockerExposedPorts ++= List(8080),
  makeBatScripts     := Nil,
  dockerUpdateLatest := true
)

lazy val root = (project in file("."))
  .settings(
    name := "password_manager"
  )
