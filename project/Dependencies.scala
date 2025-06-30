import sbt.*

object Dependencies {

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"   %% "api-test-runner" % "0.9.0"  % Test,
    "org.scalatest" %% "scalatest"       % "3.2.19" % Test
  )

}
