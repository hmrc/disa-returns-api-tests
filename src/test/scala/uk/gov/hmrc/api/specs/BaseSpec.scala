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
import uk.gov.hmrc.api.service.{InitialiseReturnsSubmissionService, MonthlyReturnsSubmissionService}
import uk.gov.hmrc.api.utils.FileReader

import java.time.LocalDate

trait BaseSpec extends AnyFeatureSpec with GivenWhenThen with Matchers with BeforeAndAfterAll {
  val authHelper                         = new AuthHelper
  val initialiseReturnsSubmissionHelper  = new InitialiseReturnsSubmissionHelper
  val disaReturnsStubHelper              = new DisaReturnsStubHelper
  val authToken: String                  = authHelper.getAuthBearerToken
  val ppnsHelper                         = new PPNSHelper
  var validClientId: String              = _
  val monthlyReturnsSubmissionService    = new MonthlyReturnsSubmissionService
  val initialiseReturnsSubmissionService = new InitialiseReturnsSubmissionService

  val currentYear: Int = LocalDate.now.getYear
  val isaReferenceId   = "Z451234"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val thirdPartyApplicationResponse: StandaloneWSResponse =
      ppnsHelper.createClientApplication(thirdpartyApplicationHadersMap)
    validClientId = FileReader.readString(thirdPartyApplicationResponse, "details", "clientId")
    ppnsHelper.createNotificationBox(validClientId, notificationBoxHadersMap)
    ppnsHelper.updateSubscriptionFields()
    ppnsHelper.updateSubscriptionFieldValues(validClientId)
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
