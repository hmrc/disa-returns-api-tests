/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.api.service

import play.api.libs.ws.DefaultBodyWritables.*
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.conf.TestEnvironment
import uk.gov.hmrc.apitestrunner.http.HttpClient

import scala.concurrent.Await
import scala.concurrent.duration.*

class DisaReturnsStubService extends HttpClient {

  val disa_returns_stub_host: String       = TestEnvironment.url("disa-returns-stub")
  val reportingWindowPath: String          = "/test-only/setup-obligation-window"
  val obligationStatusFalseUrlPath: String = "/etmp/open-obligation-status/"
  val obligationStatusTrueUrlPath: String  = "/etmp/close-obligation-status/"

  def setReportingWindow(status: Boolean): StandaloneWSResponse = {
    val payload =
      s"""
         |{
         |  "reportingWindowOpen": $status
         |}
         |""".stripMargin
    Await.result(
      mkRequest(disa_returns_stub_host + reportingWindowPath)
        .withHttpHeaders("Content-Type" -> "application/json")
        .post(payload),
      10.seconds
    )
  }

  def setNoObligation(isaReference: String): StandaloneWSResponse =
    Await.result(
      mkRequest(disa_returns_stub_host + obligationStatusFalseUrlPath + isaReference)
        .post(""),
      10.seconds
    )

  def setObligationStatusTrue(isaReference: String): StandaloneWSResponse =
    Await.result(
      mkRequest(disa_returns_stub_host + obligationStatusTrueUrlPath + isaReference)
        .post(""),
      10.seconds
    )
}
