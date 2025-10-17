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

import org.scalactic.Prettifier.default
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, GivenWhenThen}
import play.api.*
import play.api.libs.json
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.helpers.*
import uk.gov.hmrc.api.service.*
import uk.gov.hmrc.api.utils.MockMonthlyReturnData.validNdjsonTestData

import java.time.LocalDate
import scala.util.{Random, Try}

trait BaseSpec extends AnyFeatureSpec with GivenWhenThen with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {
  val authHelper: AuthHelper                                                 = new AuthHelper
  val authToken: String                                                      = authHelper.getAuthBearerToken
  val ppnsService: PPNSService                                               = new PPNSService
  var clientId: String                                                       = _
  val disaReturnsStubService: DisaReturnsStubService                         = new DisaReturnsStubService
  val initialiseReturnsSubmissionService: InitialiseReturnsSubmissionService = new InitialiseReturnsSubmissionService
  val monthlyReturnsSubmission: MonthlyReturnsSubmissionService              = new MonthlyReturnsSubmissionService
  val monthlyReturnsDeclaration: MonthlyReturnsDeclarationService            = new MonthlyReturnsDeclarationService
  val reportingService: ReconciliationReportService                          = new ReconciliationReportService
  val currentYear: Int                                                       = LocalDate.now.getYear
  val taxYear: String                                                        = s"$currentYear-${(currentYear + 1).toString.takeRight(2)}"
  val randomNumber                                                           = new Random()
  val generateRandomZReference: () => String                                 = () => ZReferenceGenerator.generate()
  val month                                                                  = "AUG"
  val isaManagerReference                                                    = "Z1234"

  def openReportingWindow(): Unit = {
    Given("The reporting window is open")
    disaReturnsStubService.setReportingWindow(true)
  }

  object ZReferenceGenerator {
    private val usedRefs = scala.collection.mutable.Set[String]()
    private val random   = new scala.util.Random()
    private val badRefs  = Set("Z1404", "Z1500")

    def generate(): String = {
      var ref   = ""
      var valid = false

      while (!valid) {
        ref = f"Z${random.nextInt(9999)}%04d"
        if (!usedRefs.contains(ref) && !badRefs.contains(ref)) {
          valid = true
        }
      }

      usedRefs += ref
      ref
    }
  }

  def createClientApplication(): Unit =
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

  def createNotificationBoxAndSubscribe(): Unit = {
    val setupSteps: Seq[(String, () => Any)] = Seq(
      "Create Notification Box"   -> (() => {
        val res = ppnsService.createNotificationBox(clientId, notificationBoxHeadersMap)
        res.status shouldBe 201
      }),
      "Create Subscription Field" -> (() => {
        val res = ppnsService.createSubscriptionField()
        res.status should (be(201) or be(200))
      })
      //        TODO commented out as this is creating two boxes. Do we want to test the callback url
//      "Create Subscription Field Values" -> (() => {
//        val res = ppnsService.createSubscriptionFieldValues(clientId)
//        res.status shouldBe 201
//      })
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
    isaManagerReference: String,
    headers: Map[String, String] = validHeaders,
    totalRecords: Int = 12,
    month: String
  ): StandaloneWSResponse =
    initialiseReturnsSubmissionService.postInitialiseReturnsSubmissionApi(
      totalRecords = totalRecords,
      month,
      taxYear = currentYear,
      isaManagerReference,
      headers = headers
    )

  def submissionRequest(
    isaManagerReference: String,
    taxYear: String = taxYear,
    headers: Map[String, String] = validHeaders,
    ndString: String = validNdjsonTestData(),
    month: String
  ): StandaloneWSResponse =
    monthlyReturnsSubmission.postSubmission(
      isaManagerReference,
      taxYear = taxYear,
      headers = headers,
      ndString = ndString,
      month = month
    )

  def declarationRequest(
    isaManagerReference: String,
    taxYear: String = taxYear,
    headers: Map[String, String] = validHeaders,
    month: String
  ): StandaloneWSResponse =
    monthlyReturnsDeclaration.postDeclaration(
      isaManagerReference,
      taxYear = taxYear,
      month = month,
      headers = headers
    )
}
