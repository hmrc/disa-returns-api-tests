#!/usr/bin/env bash

ENVIRONMENT=$1

sbt clean -Denvironment="${ENVIRONMENT:=development}" "testOnly uk.gov.hmrc.api.specs.*"