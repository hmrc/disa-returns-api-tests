/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.api.utils

import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen}
import play.api.*
import play.api.libs.json
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.helpers.*
import uk.gov.hmrc.api.service.*
import uk.gov.hmrc.api.utils.MockMonthlyReturnData.validNdjsonTestData

import java.time.LocalDate
import scala.util.Try

trait BaseSpec extends AnyFeatureSpec with GivenWhenThen with Matchers with BeforeAndAfterAll {
  val authHelper: AuthHelper                                                 = new AuthHelper
  val authToken: String                                                      = authHelper.getAuthBearerToken
  val ppnsService: PPNSService                                               = new PPNSService
  var clientId: String                                                       = _
  val disaReturnsStubService: DisaReturnsStubService                         = new DisaReturnsStubService
  val initialiseReturnsSubmissionService: InitialiseReturnsSubmissionService = new InitialiseReturnsSubmissionService
  val monthlyReturnsSubmissionService: MonthlyReturnsSubmissionService       = new MonthlyReturnsSubmissionService
  val completeMonthlyReturnsService: CompleteMonthlyReturns                  = new CompleteMonthlyReturns
  val reportingService: ReportingService                                     = new ReportingService

  val currentYear: Int       = LocalDate.now.getYear
  val isaReferenceId: String = "Z4512"

  override def beforeAll(): Unit = {
    super.beforeAll()

    withClue("Setup step failed: Create Client Application → ") {
      val thirdPartyApplicationResponse =
        ppnsService.createClientApplication(thirdPartyApplicationHeadersMap)
      thirdPartyApplicationResponse.status should (be(201) or be(200))

      val jsonTry = Try(Json.parse(thirdPartyApplicationResponse.body))
      jsonTry.fold(
        _ => fail("Response body was not valid JSON."),
        json =>
          clientId = (json \ "details" \ "token" \ "clientId").asOpt[String].getOrElse {
            fail("Could not extract clientId from response JSON.")
          }
      )
    }

    val setupSteps: Seq[(String, () => Any)] = Seq(
      "Create Notification Box"          -> (() => {
        val res = ppnsService.createNotificationBox(clientId, notificationBoxHeadersMap)
        res.status shouldBe 201
      }),
      "Create Subscription Field"        -> (() => {
        val res = ppnsService.createSubscriptionField()
        res.status should (be(201) or be(200))
      }),
      "Create Subscription Field Values" -> (() => {
        val res = ppnsService.createSubscriptionFieldValues(clientId)
        res.status shouldBe 201
      })
    )
    setupSteps.foreach { case (name, action) =>
      withClue(s"Setup step failed: $name → ") {
        action()
      }
    }
  }

  def validHeaders: Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "X-Client-ID"   -> clientId,
    "Authorization" -> authToken
  )

  def validHeadersOnlyWithToken: Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "Authorization" -> authToken
  )

  val headersIncorrectBearerToken: Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "X-Client-ID"   -> clientId,
    "Authorization" -> "BadAuthToken"
  )

  def headersMapWithValidClientIDAndTokenWithoutContentType: Map[String, String] = Map(
    "X-Client-ID"   -> clientId,
    "Authorization" -> authToken
  )

  val thirdPartyApplicationHeadersMap: Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "Authorization" -> authToken
  )

  val notificationBoxHeadersMap: Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "Authorization" -> authToken,
    "User-Agent"    -> "disa-returns"
  )

  def postInitiateReturnsSubmission(
    isaManagerReference: String = isaReferenceId,
    headers: Map[String, String] = validHeaders,
    totalRecords: Int = 12
  ): StandaloneWSResponse =
    initialiseReturnsSubmissionService.postInitialiseReturnsSubmissionApi(
      totalRecords = totalRecords,
      submissionPeriod = "APR",
      taxYear = currentYear,
      isManagerReference = isaManagerReference,
      headers = headers
    )

  def postMonthlyReturnsSubmission(
    isaManagerReference: String = isaReferenceId,
    returnId: String,
    headers: Map[String, String] = validHeaders,
    ndString: String = validNdjsonTestData()
  ): StandaloneWSResponse =
    monthlyReturnsSubmissionService.postMonthlyReturnsSubmission(
      isaManagerReference = isaManagerReference,
      returnId = returnId,
      headers = headers,
      ndString = ndString
    )

  def postCompleteMonthlyReturns(
    isaManagerReference: String = isaReferenceId,
    headers: Map[String, String] = validHeadersOnlyWithToken,
    returnId: String
  ): StandaloneWSResponse =
    completeMonthlyReturnsService.postCompleteMonthlyReturns(
      isaManagerReference = isaManagerReference,
      returnId = returnId,
      headers = headers
    )
}
