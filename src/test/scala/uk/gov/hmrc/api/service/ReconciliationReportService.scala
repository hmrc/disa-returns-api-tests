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

import play.api.libs.ws.DefaultBodyWritables.writeableOf_String
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.constant.AppConfig
import uk.gov.hmrc.api.constant.AppConfig.*
import uk.gov.hmrc.apitestrunner.http.HttpClient

import scala.concurrent.Await
import scala.concurrent.duration.*

class ReconciliationReportService extends HttpClient {
  val reportingResultsSummaryPath: String = "/results/summary"
  val testSupportPath: String             = "/reconciliation"

  def makeReturnSummaryCallback(
    isaManagerReference: String,
    taxYear: String,
    month: String,
    totalRecords: Int,
    headers: Map[String, String]
  ): StandaloneWSResponse = {
    val payload =
      s"""
         |{
         |  "totalRecords": $totalRecords
         |}
         |""".stripMargin
    Await.result(
      mkRequest(disaReturnsHost + disaReturnsCallbackPath + isaManagerReference + "/" + taxYear + "/" + month)
        .withHttpHeaders(headers.toSeq: _*)
        .post(payload),
      10.seconds
    )
  }

  def triggerReportReadyScenario(
    isaManagerReference: String,
    taxYear: String,
    month: String,
    numbers: Array[Int],
    headers: Map[String, String]
  ): StandaloneWSResponse = {
    val payload =
      s"""
         {
         |    "oversubscribed": ${numbers(0)},
         |    "traceAndMatch": ${numbers(1)},
         |    "failedEligibility": ${numbers(2)}
         |}""".stripMargin
    Await.result(
      mkRequest(
        disaReturnsTestSupportHost + "/" + isaManagerReference + "/" + taxYear + "/" + month + testSupportPath
      )
        .withHttpHeaders(headers.toSeq: _*)
        .post(payload),
      10.seconds
    )
  }

  def getReportingResultsSummary(
    isaManagerReference: String,
    taxYear: String,
    month: String,
    headers: Map[String, String]
  ): StandaloneWSResponse =
    Await.result(
      mkRequest(
        disaReturnsHost + disaReturnsRoute + isaManagerReference + "/" + taxYear + "/" + month + reportingResultsSummaryPath
      )
        .withHttpHeaders(headers.toSeq: _*)
        .get(),
      10.seconds
    )

  def getReconcilationReportPage(
    isaManagerReference: String,
    taxYear: String,
    month: String,
    page: Int,
    headers: Map[String, String]
  ): StandaloneWSResponse =
    Await.result(
      mkRequest(
        disaReturnsHost + disaReturnsRoute + isaManagerReference + "/" + taxYear + "/" + month + "/results?page=" + page
      )
        .withHttpHeaders(headers.toSeq: _*)
        .get(),
      10.seconds
    )
}
