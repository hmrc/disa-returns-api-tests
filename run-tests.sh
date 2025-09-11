#!/usr/bin/env bash

ENVIRONMENT=$1


sbt scalafmtCheckAll scalafmtSbtCheck
sbt clean -Denvironment="${ENVIRONMENT:=local}" "testOnly uk.gov.hmrc.api.specs.*"