import sbt.*

object Dependencies {

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                %% "api-test-runner" % "0.10.0" % Test,
    "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.5"  % Test
  )

}
