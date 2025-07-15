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

import play.api.libs.json.{JsValue, Json, Writes}
import play.api.libs.ws.DefaultBodyWritables.writeableOf_String
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.conf.TestEnvironment
import uk.gov.hmrc.api.utils.FileReader.readLines
import uk.gov.hmrc.api.utils.{JsonGenerator, SubPathGenerator}
import uk.gov.hmrc.apitestrunner.http.HttpClient

import scala.concurrent.Await
import scala.concurrent.duration.*

class DisaReturnService extends HttpClient {
  val host: String                    = TestEnvironment.url("disa-returns")
  val monthlyReturnSubPath: String    = SubPathGenerator.generateReturnPath()
  val monthlyReturnHeaderPath: String = SubPathGenerator.generateHeaderPath()
  val monthlyReturnFilename           = "Submission1"

  def postHeader(
    totalRecords: Int,
    submissionPeriod: String,
    taxYear: Int,
    bearerToken: String
  ): StandaloneWSResponse = {
    implicit val returnsHeaderWrites: Writes[ReturnsHeader] = Json.writes[ReturnsHeader]
    val payload                                             = ReturnsHeader(totalRecords, submissionPeriod, taxYear)
    val jsonString: JsValue                                 = Json.toJson(payload)

    Await.result(
      mkRequest(host + monthlyReturnHeaderPath)
        .withHttpHeaders(
          "Content-Type"  -> "application/json",
          "X-Client-ID"   -> "s87xrlwgySiCFK2zfWdOlOYcSlj7",
          "Authorization" -> bearerToken
        )
        .post(jsonString),
      10.seconds
    )
  }

  def postReturns(): StandaloneWSResponse = {
    val payload      = readLines(monthlyReturnFilename)
    val ndjsonString = JsonGenerator.generateSerializedNdjson(payload)
    Await.result(
      mkRequest(host + monthlyReturnSubPath)
        .withHttpHeaders("Content-Type" -> "application/x-ndjson")
        .post(ndjsonString),
      10.seconds
    )
  }

  case class ReturnsHeader(totalRecords: Int, submissionPeriod: String, taxYear: Int)
  case class SubmissionResponse(returnId: String, action: String)
}
