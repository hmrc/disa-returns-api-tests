#!/usr/bin/env bash

ENVIRONMENT=$1

sbt clean -Denvironment="${ENVIRONMENT:=qa}" "testOnly uk.gov.hmrc.api.specs.*"