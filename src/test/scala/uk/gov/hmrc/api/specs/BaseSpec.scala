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

package uk.gov.hmrc.api.specs

import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen}
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.helpers.*
import uk.gov.hmrc.api.service.*
import uk.gov.hmrc.api.utils.FileReader

import java.time.LocalDate

trait BaseSpec extends AnyFeatureSpec with GivenWhenThen with Matchers with BeforeAndAfterAll {
  val authHelper                         = new AuthHelper
  val authToken: String                  = authHelper.getAuthBearerToken
  val ppnsService                        = new PPNSService
  var validClientId: String              = _
  val disaReturnsStubService             = new DisaReturnsStubService
  val initialiseReturnsSubmissionService = new InitialiseReturnsSubmissionService
  val monthlyReturnsSubmissionService    = new MonthlyReturnsSubmissionService
  val completeMonthlyReturnsService      = new CompleteMonthlyReturns

  val currentYear: Int = LocalDate.now.getYear
  val isaReferenceId   = "Z4512"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val thirdPartyApplicationResponse: StandaloneWSResponse =
      ppnsService.createClientApplication(thirdpartyApplicationHadersMap)
    validClientId = FileReader.readString(thirdPartyApplicationResponse, "details", "clientId")
    println(Console.RED + s"validClientId = $validClientId" + Console.RESET)
    ppnsService.createNotificationBox(validClientId, notificationBoxHadersMap)
    ppnsService.createSubscriptionField()
    ppnsService.createSubscriptionFieldValues(validClientId)
  }

  val headersMap: Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "Authorization" -> authToken
  )

  def validHeaders: Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "X-Client-ID"   -> validClientId,
    "Authorization" -> authToken
  )

  def validHeadersOnlyWithToken: Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "Authorization" -> authToken
  )

  def validHeadersWithInvalidToken: Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "Authorization" -> "authToken"
  )

  val headersIncorrectBearerToken: Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "X-Client-ID"   -> validClientId,
    "Authorization" -> "BadAuthToken"
  )

  def headersMapWithValidClientIDAndTokenWithoutContentType: Map[String, String] = Map(
    "X-Client-ID"   -> validClientId,
    "Authorization" -> authToken
  )

  val headersMapWithIncorrectClientId: Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "X-Client-ID"   -> "123456",
    "Authorization" -> authToken
  )

  val headersMapWithIncorrectClientIdAndIncorrectToken: Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "X-Client-ID"   -> "123456",
    "Authorization" -> "authToken"
  )

  val invalidHeadersMap: Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "Authorization" -> "authToken"
  )

  val thirdpartyApplicationHadersMap: Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "Authorization" -> authToken
  )

  val notificationBoxHadersMap: Map[String, String] = Map(
    "Content-Type"  -> "application/json",
    "Authorization" -> authToken,
    "User-Agent"    -> "disa-returns"
  )
}
