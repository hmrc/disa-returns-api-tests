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

import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.DefaultBodyWritables.writeableOf_String
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.conf.TestEnvironment
import uk.gov.hmrc.api.models.InitialiseReturnsSubmissionPayload
import uk.gov.hmrc.api.utils.FileReader.readLines
import uk.gov.hmrc.api.utils.{JsonGenerator, SubPathGenerator}
import uk.gov.hmrc.apitestrunner.http.HttpClient

import scala.concurrent.Await
import scala.concurrent.duration.*

class DisaReturnService extends HttpClient {
  val disa_returns_host: String                     = TestEnvironment.url("disa-returns")
  val submissionBulkMonthlyReturnApiSubpath: String = SubPathGenerator.generateReturnPath()
  val initializeReturnsSubmissionApiPath: String    = "/init"
  val bulkMonthlyReturnFileName                     = "Submission1"

  def postInitialiseReturnsSubmissionApi(
    totalRecords: Int,
    submissionPeriod: String,
    taxYear: Int,
    isManagerReference: String,
    headers: Map[String, String]
  ): StandaloneWSResponse = {
    val payload             = InitialiseReturnsSubmissionPayload(totalRecords, submissionPeriod, taxYear)
    val jsonString: JsValue = Json.toJson(payload)

    Await.result(
      mkRequest(disa_returns_host + s"$isManagerReference" + initializeReturnsSubmissionApiPath)
        .withHttpHeaders(headers.toSeq: _*)
        .post(jsonString),
      10.seconds
    )
  }

  def submitBulkMonthlyReturns(isaManagerReference: String, returnId: String): StandaloneWSResponse = {
    val bulkMonthlyReturn                = readLines(bulkMonthlyReturnFileName)
    val ndjsonBulkMonthlyReturnAsaString = JsonGenerator.generateSerializedNdjson(bulkMonthlyReturn)
    Await.result(
      mkRequest(disa_returns_host + "/" + isaManagerReference + "/" + returnId)
        .withHttpHeaders("Content-Type" -> "application/x-ndjson")
        .post(ndjsonBulkMonthlyReturnAsaString),
      10.seconds
    )
  }
}
