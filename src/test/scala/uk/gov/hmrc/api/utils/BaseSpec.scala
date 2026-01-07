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
  val authHelper: AuthHelper                                      = new AuthHelper
  val ppnsService: PPNSService                                    = new PPNSService
  var clientId: String                                            = _
  val disaReturnsStubService: DisaReturnsStubService              = new DisaReturnsStubService
  val monthlyReturnsSubmission: MonthlyReturnsSubmissionService   = new MonthlyReturnsSubmissionService
  val monthlyReturnsDeclaration: MonthlyReturnsDeclarationService = new MonthlyReturnsDeclarationService
  val reportingService: ReconciliationReportService               = new ReconciliationReportService
  val currentYear: Int                                            = LocalDate.now.getYear
  val taxYear: String                                             = s"$currentYear-${(currentYear + 1).toString.takeRight(2)}"
  val randomNumber                                                = new Random()
  val generateRandomZReference: () => String                      = () => ZReferenceGenerator.generate()
  val month                                                       = "AUG"
  val totalRecords                                                = Array(1, 2, 3)

  def openReportingWindow(): Unit = {
    Given("The reporting window is open")
    disaReturnsStubService.setReportingWindow(true)
  }

  object ZReferenceGenerator {
    private val usedRefs = scala.collection.mutable.Set[String]()
    private val random   = new scala.util.Random()

    def generate(): String = {
      var ref   = ""
      var valid = false

      while (!valid) {
        ref = f"Z${random.nextInt(9999)}%04d"
        if (!usedRefs.contains(ref)) {
          valid = true
        }
      }

      usedRefs += ref
      ref
    }
  }

  def createClientApplication(token: String): Unit =
    withClue("Setup step failed: Create Client Application → ") {
      val headers: Map[String, String]  = thirdPartyApplicationHeadersMap(token)
      val thirdPartyApplicationResponse =
        ppnsService.createClientApplication(headers)
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

  def createNotificationBoxAndSubscribe(token: String): Unit = {
    val headers: Map[String, String]         = notificationBoxHeadersMap(token)
    val setupSteps: Seq[(String, () => Any)] = Seq(
      "Create Notification Box"   -> (() => {
        val res = ppnsService.createNotificationBox(clientId, headers)
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

  def validHeaders(token: String): Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "X-Client-ID"   -> clientId,
    "Authorization" -> token
  )

  def validHeadersOnlyWithToken(token: String): Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "Authorization" -> token
  )

  val headersIncorrectBearerToken: Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "X-Client-ID"   -> clientId,
    "Authorization" -> "BadAuthToken"
  )

  def headersMapWithValidClientIDAndTokenWithoutContentType(token: String): Map[String, String] = Map(
    "X-Client-ID"   -> clientId,
    "Authorization" -> token
  )

  def thirdPartyApplicationHeadersMap(token: String): Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "Authorization" -> token
  )

  def notificationBoxHeadersMap(token: String): Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "Authorization" -> token,
    "User-Agent"    -> "disa-returns"
  )

  def submissionRequest(
    token: String,
    isaManagerReference: String,
    taxYear: String = taxYear,
    ndString: String = validNdjsonTestData(),
    month: String
  ): StandaloneWSResponse =
    val headers: Map[String, String] = validHeaders(token)
    monthlyReturnsSubmission.postSubmission(
      isaManagerReference,
      taxYear = taxYear,
      headers = headers,
      ndString = ndString,
      month = month
    )

  def declarationRequest(
    token: String,
    isaManagerReference: String,
    taxYear: String = taxYear,
    month: String,
    nilReturn: Boolean = false
  ): StandaloneWSResponse =
    val headers: Map[String, String] = validHeaders(token)
    monthlyReturnsDeclaration.postDeclaration(
      isaManagerReference,
      taxYear = taxYear,
      month = month,
      headers = headers,
      nilReturn
    )
}
