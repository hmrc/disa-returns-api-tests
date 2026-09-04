lazy val root = (project in file("."))
  .settings(
    name := "disa-returns-api-tests",
    version := "0.1.0",
    scalaVersion := "3.3.6",
    libraryDependencies ++= Dependencies.test,
    Test / javaOptions ++= Seq(
      "-Dlogger.resource=logback-test.xml",
      s"-Denvironment=${sys.props.getOrElse("environment", "local")}"
    ),
    Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,
    Test / fork := true,
    (Compile / compile) := ((Compile / compile) dependsOn (Compile / scalafmtSbtCheck, Compile / scalafmtCheckAll)).value
  )

addCommandAlias("precommit", ";scalafmtAll;scalafmtSbt;test")
