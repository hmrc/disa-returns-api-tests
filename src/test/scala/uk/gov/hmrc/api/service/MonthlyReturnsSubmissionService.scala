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

import play.api.libs.json.Json
import play.api.libs.ws.DefaultBodyWritables.writeableOf_String
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.conf.TestEnvironment
import uk.gov.hmrc.api.models.*
import uk.gov.hmrc.api.utils.RandomDataGenerator
import uk.gov.hmrc.apitestrunner.http.HttpClient

import scala.concurrent.Await
import scala.concurrent.duration.*

class MonthlyReturnsSubmissionService extends HttpClient {
  val disa_returns_host: String = TestEnvironment.url("disa-returns")

  def getLISANewSubscriptionPayload(
    nino: String,
    accountNumber: String
  ): LISANewSubscriptionPayload = LISANewSubscriptionPayload(
    accountNumber,
    nino,
    "First24997",
    "LISA NS model",
    "Last24997",
    "1980-01-22",
    "LIFETIME_CASH",
    false,
    "2025-06-01",
    2500.23,
    10000.12,
    "2025-01-22",
    5000.56,
    3000.56
  )

  def getLISAClosurePayload(
    nino: String,
    accountNumber: String
  ): LISAClosurePayload = LISAClosurePayload(
    accountNumber,
    nino,
    "First24998",
    "LISA Closure model",
    "Last24998",
    "1980-01-22",
    "LIFETIME_STOCKS_AND_SHARES",
    false,
    "2025-06-01",
    2500.23,
    10000.12,
    "2025-01-22",
    "2025-03-22",
    "CLOSED",
    5000.56,
    3000.56
  )

  def getLISATransferAndClosurePayload(
    nino: String,
    accountNumber: String
  ): LISATransferAndClosurePayload = LISATransferAndClosurePayload(
    accountNumber,
    nino,
    "First24999",
    "LISA Transfer & Closure model",
    "Last24999",
    "1980-01-22",
    "LIFETIME_STOCKS_AND_SHARES",
    true,
    "2025-06-01",
    2500.23,
    10000.12,
    "1234567",
    1200.34,
    "2025-01-22",
    "2025-03-22",
    "TRANSFERRED_IN_FULL",
    5000.56,
    3000.56
  )

  def getLISATransferPayload(
    nino: String,
    accountNumber: String
  ): LISATransferPayload = LISATransferPayload(
    accountNumber,
    nino,
    "First25000",
    "LISA Transfer model",
    "Last25000",
    "1980-01-22",
    "LIFETIME_CASH",
    true,
    "2025-06-01",
    2500.23,
    10000.12,
    "1234567",
    1200.34,
    "2025-01-22",
    5000.56,
    3000.56
  )

  def getSISANewSubscriptionPayload(
    nino: String,
    accountNumber: String
  ): SISANewSubscriptionPayload = SISANewSubscriptionPayload(
    accountNumber,
    nino,
    "First25001",
    "SISA new subscription model",
    "Last25001",
    "1980-01-22",
    "STOCKS_AND_SHARES",
    false,
    "2025-06-01",
    2500.23,
    10000.12,
    false
  )

  def getSISATransferPayload(
    nino: String,
    accountNumber: String
  ): SISATransferPayload = SISATransferPayload(
    accountNumber,
    nino,
    "First25002",
    "SISA Transfer model",
    "Last25002",
    "1980-01-22",
    "CASH",
    true,
    "2025-06-01",
    2500.23,
    10000.12,
    "1234567",
    1200.34,
    true
  )

  def postMonthlyReturnsSubmission(
    isaManagerReference: String,
    returnId: String,
    headers: Map[String, String]
  ): StandaloneWSResponse = {
    val lisaNewSubscriptionPayload    =
      getLISANewSubscriptionPayload(RandomDataGenerator.nino(), RandomDataGenerator.generateSTDCode())
    val lisaClosurePayload            = getLISAClosurePayload(RandomDataGenerator.nino(), RandomDataGenerator.generateSTDCode())
    val lisaTransferAndClosurePayload =
      getLISATransferAndClosurePayload(RandomDataGenerator.nino(), RandomDataGenerator.generateSTDCode())
    val lisaTransferPayload           = getLISATransferPayload(RandomDataGenerator.nino(), RandomDataGenerator.generateSTDCode())
    val sisaNewSubscriptionPayload    =
      getSISANewSubscriptionPayload(RandomDataGenerator.nino(), RandomDataGenerator.generateSTDCode())
    val sisaTransferPayload           = getSISATransferPayload(RandomDataGenerator.nino(), RandomDataGenerator.generateSTDCode())

    val jsonString =
      Json.toJson(lisaNewSubscriptionPayload).toString + "\n" + Json.toJson(lisaClosurePayload).toString + "\n" + Json
        .toJson(lisaTransferAndClosurePayload)
        .toString + "\n" + Json.toJson(lisaTransferPayload).toString + "\n" + Json
        .toJson(sisaNewSubscriptionPayload)
        .toString + "\n" + Json.toJson(sisaTransferPayload).toString + "\n"
    println("--------" + "\n" + jsonString)
    Await.result(
      mkRequest(disa_returns_host + isaManagerReference + "/" + returnId)
        .withHttpHeaders(headers.toSeq: _*)
        .post(jsonString),
      10.seconds
    )
  }
}
