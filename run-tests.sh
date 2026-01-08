#!/usr/bin/env bash

ENVIRONMENT=$1

sbt clean -Denvironment="${ENVIRONMENT:=dev}" "testOnly uk.gov.hmrc.api.specs.*"