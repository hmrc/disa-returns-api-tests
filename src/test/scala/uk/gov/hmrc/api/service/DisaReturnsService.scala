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

import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.MimeTypes.JSON
import play.api.libs.json.*
import play.api.libs.ws.DefaultBodyWritables.writeableOf_String
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.conf.TestEnvironment
import uk.gov.hmrc.apitestrunner.http.HttpClient

import java.time.Instant
import scala.concurrent.Await
import scala.concurrent.duration.*

class DisaReturnsService extends HttpClient {

  private lazy val disaReturnsHost: String           = TestEnvironment.url("disa-returns")
  private lazy val disaReturnsSubmissionHost: String = TestEnvironment.url("disa-returns-submission")
  private lazy val disaReturnsPath: String           = "/monthly"
  private lazy val disaReturnsCallbackPath: String   = "/callback/monthly"
  private lazy val disaReturnsBase: String           = s"$disaReturnsHost$disaReturnsPath"

  def postSubmission(
    isaManagerReference: String,
    headers: Map[String, String],
    ndString: String = ""
  ): StandaloneWSResponse =
    Await.result(
      mkRequest(s"$disaReturnsBase/$isaManagerReference")
        .withHttpHeaders(headers.toSeq: _*)
        .post(ndString),
      10.seconds
    )

  def postDeclaration(
    isaManagerReference: String,
    headers: Map[String, String],
    nilReturn: Boolean
  ): StandaloneWSResponse = {

    val body = Json.stringify(Json.obj("nilReturn" -> nilReturn))
    Await.result(
      mkRequest(s"$disaReturnsBase/$isaManagerReference/declaration")
        .withHttpHeaders(headers.toSeq: _*)
        .post(body),
      10.seconds
    )
  }

  def getReconciliationReport(
    isaManagerReference: String,
    headers: Map[String, String],
    cursor: Option[String] = None,
    limit: Option[Int] = None
  ): StandaloneWSResponse = {
    val queryParameters = Seq(
      cursor.map("cursor" -> _),
      limit.map(value => "limit" -> value.toString)
    ).flatten

    Await.result(
      mkRequest(s"$disaReturnsBase/$isaManagerReference/results")
        .addQueryStringParameters(queryParameters: _*)
        .withHttpHeaders(headers.toSeq: _*)
        .get(),
      10.seconds
    )
  }

  def setReportingWindowOverride(
    isaManagerReference: String,
    startDate: Instant,
    endDate: Instant,
    clockDate: String
  ): StandaloneWSResponse = {
    val payload = Json.stringify(
      Json.obj(
        "zReferences"     -> Seq(isaManagerReference),
        "clock"           -> Json.obj("date" -> clockDate),
        "reportingWindow" -> Json.obj(
          "startDate" -> startDate.toString,
          "endDate"   -> endDate.toString
        )
      )
    )

    Await.result(
      mkRequest(s"$disaReturnsSubmissionHost/test-only/overrides")
        .withHttpHeaders(CONTENT_TYPE -> JSON)
        .put(payload),
      10.seconds
    )
  }

  def deleteOverrides(isaManagerReferences: Seq[String]): StandaloneWSResponse = {
    val payload = Json.stringify(Json.obj("zReferences" -> isaManagerReferences))

    Await.result(
      mkRequest(s"$disaReturnsSubmissionHost/test-only/overrides/delete")
        .withHttpHeaders(CONTENT_TYPE -> JSON)
        .post(payload),
      10.seconds
    )
  }

  def setClock(isaManagerReference: String, date: String): StandaloneWSResponse = {
    val payload = Json.stringify(
      Json.obj(
        "zReferences"     -> Seq(isaManagerReference),
        "clock"           -> Json.obj("date" -> date),
        "reportingWindow" -> None
      )
    )

    Await.result(
      mkRequest(s"$disaReturnsSubmissionHost/test-only/overrides")
        .withHttpHeaders(CONTENT_TYPE -> JSON)
        .put(payload),
      10.seconds
    )
  }

  def makeReconciliationReportReadyCallback(
    isaManagerReference: String,
    headers: Map[String, String]
  ): StandaloneWSResponse =
    Await.result(
      mkRequest(s"$disaReturnsHost$disaReturnsCallbackPath/$isaManagerReference")
        .withHttpHeaders(headers.toSeq: _*)
        .post(""),
      10.seconds
    )
}
