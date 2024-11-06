import sbt._

object Dependencies {

  object V {
    val cats          = "2.10.0"
    val catsEffect    = "3.5.4"
    val circe         = "0.14.6"

    val doobie        = "1.0.0-RC4"
    val flyway        = "8.5.13"
    val fs2Core       = "3.9.4"
    val fs2Kafka      = "3.4.0"

    val SttpApispec        = "0.8.0"
    val SttpClient3        = "3.9.5"
    val SttpModel          = "1.7.9"
    val SttpShared         = "1.3.17"
    val SttpTapir          = "1.10.0"

    val ip4s          = "3.4.0"
    val iron          = "2.6.0"
    val kittens       = "3.3.0"
    val log4cats      = "2.7.0"
    val monocle       = "3.2.0"
    val natchez       = "0.3.5"
    val natchezHttp4s = "0.5.0"
    val neutron       = "0.8.0"
    val odin          = "0.13.0"
    val redis4cats    = "1.7.1"
    val refined       = "0.11.1"

    val scalacheck = "1.18.0"
    val weaver     = "0.8.4"

    val organizeImports = "0.6.0"
    val zerowaste       = "0.2.21"
  }

  object Libraries {
    def circe(artifact: String) = Def.setting("io.circe" %% s"circe-$artifact" % V.circe)

    val cats       = Def.setting("org.typelevel" %% "cats-core" % V.cats)
    val catsEffect = Def.setting("org.typelevel" %% "cats-effect" % V.catsEffect)
    val fs2Core    = Def.setting("co.fs2" %% "fs2-core" % V.fs2Core)
    val kittens    = Def.setting("org.typelevel" %% "kittens" % V.kittens)

    val circeCore    = circe("core")
    val circeParser  = circe("parser")
    val circeRefined = circe("refined")

    val doobieH2 = "org.tpolecat" %% "doobie-h2"       % V.doobie
    val doobiePg = "org.tpolecat" %% "doobie-postgres" % V.doobie
    val flyway   = "org.flywaydb"  % "flyway-core"     % V.flyway

    val tapirCore = Seq(
      "com.softwaremill.sttp.model" %% "core"             % V.SttpModel,
      "com.softwaremill.sttp.tapir" %% "tapir-core"       % V.SttpTapir,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % V.SttpTapir,
    )

    val tapirMetrics = Seq("com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % V.SttpTapir)

    val tapirServer = Seq(
      "com.softwaremill.sttp.apispec" %% "openapi-model"           % V.SttpApispec,
      "com.softwaremill.sttp.shared"  %% "fs2"                     % V.SttpShared,
      "com.softwaremill.sttp.tapir"   %% "tapir-http4s-server"     % V.SttpTapir,
      "com.softwaremill.sttp.tapir"   %% "tapir-openapi-docs"      % V.SttpTapir,
      "com.softwaremill.sttp.tapir"   %% "tapir-server"            % V.SttpTapir,
      "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui"        % V.SttpTapir,
      "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle" % V.SttpTapir,
    )

    val ironCore  = Def.setting("io.github.iltotore" %% "iron" % V.iron)
    val ironCats  = Def.setting("io.github.iltotore" %% "iron-cats" % V.iron)
    val ironCirce = Def.setting("io.github.iltotore" %% "iron-circe" % V.iron)

    val ip4sCore = Def.setting("com.comcast" %% "ip4s-core" % V.ip4s)

    val natchezCore      = "org.tpolecat" %% "natchez-core"      % V.natchez
    val natchezHoneycomb = "org.tpolecat" %% "natchez-honeycomb" % V.natchez
    val natchezHttp4s    = "org.tpolecat" %% "natchez-http4s"    % V.natchezHttp4s

    val neutronCore       = "dev.profunktor" %% "neutron-core"       % V.neutron
    val redis4catsEffects = "dev.profunktor" %% "redis4cats-effects" % V.redis4cats

    val monocleCore = Def.setting("dev.optics" %% "monocle-core" % V.monocle)

    val odin = "com.github.valskalla" %% "odin-core" % V.odin

    val log4catsNoop = "org.typelevel" %% "log4cats-noop" % V.log4cats

    val catsLaws         = "org.typelevel"       %% "cats-laws"         % V.cats
    val monocleLaw       = "dev.optics"          %% "monocle-law"       % V.monocle
    val scalacheck       = "org.scalacheck"      %% "scalacheck"        % V.scalacheck
    val weaverCats       = "com.disneystreaming" %% "weaver-cats"       % V.weaver
    val weaverDiscipline = "com.disneystreaming" %% "weaver-discipline" % V.weaver
    val weaverScalaCheck = "com.disneystreaming" %% "weaver-scalacheck" % V.weaver

    val fs2Kafka    = "com.github.fd4s" %% "fs2-kafka" % V.fs2Kafka
    val refinedCore = Def.setting("eu.timepit" %% "refined" % V.refined)
    val refinedCats = Def.setting("eu.timepit" %% "refined-cats" % V.refined)
  }

  object CompilerPlugins {
    val zerowaste = compilerPlugin("com.github.ghik" % "zerowaste" % V.zerowaste cross CrossVersion.full)
  }

}