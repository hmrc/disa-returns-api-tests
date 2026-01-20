import sbt.*

object Dependencies {

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                %% "api-test-runner" % "0.10.0" % Test,
    "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.5"  % Test,
    "org.slf4j"                   % "slf4j-api"       % "2.0.13" % Test,
    "ch.qos.logback"              % "logback-classic" % "1.4.14" % Test,
    "io.circe"                   %% "circe-core"      % "0.14.6" % Test,
    "io.circe"                   %% "circe-generic"   % "0.14.6" % Test,
    "io.circe"                   %% "circe-parser"    % "0.14.6" % Test,
    "org.jsoup"                   % "jsoup"           % "1.16.1" % Test
  )
}
