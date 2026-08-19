/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.http.Status.{BAD_REQUEST, FORBIDDEN, NO_CONTENT}
import play.api.libs.json.Json
import uk.gov.hmrc.api.constant.AppConfig.env
import uk.gov.hmrc.api.utils.BaseSpec

import java.time.Instant
import java.util.UUID

class ReportingWindowOverrideSpec extends BaseSpec {

  Scenario("1. An active reporting-window override allows a monthly return submission") {
    assumeOverridesSupported()

    Given("I have valid authentication with an isolated credential ID")
    val isaReference = generateRandomZReference()
    val authToken    = authHelper.getAuthBearerToken(isaReference, uniqueCredentialId())
    val now          = Instant.now()

    When("I configure an override containing the current instant")
    val overrideResponse = setOverride(authToken, isaReference, now.minusSeconds(60), now.plusSeconds(300))

    Then("The override is accepted")
    overrideResponse.status shouldBe NO_CONTENT

    And("A monthly return can be submitted")
    submissionRequest(authToken, isaReference).status shouldBe NO_CONTENT
  }

  Scenario("2. An inactive reporting-window override prevents a monthly return submission") {
    assumeOverridesSupported()

    Given("I have valid authentication with an isolated credential ID")
    val isaReference = generateRandomZReference()
    val authToken    = authHelper.getAuthBearerToken(isaReference, uniqueCredentialId())
    val now          = Instant.now()

    When("I configure an override that starts in the future")
    val overrideResponse = setOverride(authToken, isaReference, now.plusSeconds(3600), now.plusSeconds(7200))

    Then("The override is accepted")
    overrideResponse.status shouldBe NO_CONTENT

    And("A monthly return is rejected because the reporting window is closed")
    val submissionResponse = submissionRequest(authToken, isaReference)
    submissionResponse.status shouldBe FORBIDDEN
    (Json.parse(submissionResponse.body) \ "code").as[String] shouldBe "REPORTING_WINDOW_CLOSED"
  }

  Scenario("3. A later reporting-window override replaces the previous override") {
    assumeOverridesSupported()

    Given("I have configured a future reporting window")
    val isaReference = generateRandomZReference()
    val authToken    = authHelper.getAuthBearerToken(isaReference, uniqueCredentialId())
    val now          = Instant.now()
    setOverride(authToken, isaReference, now.plusSeconds(3600), now.plusSeconds(7200)).status shouldBe NO_CONTENT
    submissionRequest(authToken, isaReference).status shouldBe FORBIDDEN

    When("I replace it with a reporting window containing the current instant")
    val replacementResponse = setOverride(authToken, isaReference, now.minusSeconds(60), now.plusSeconds(300))

    Then("The replacement is accepted and a monthly return can be submitted")
    replacementResponse.status shouldBe NO_CONTENT
    submissionRequest(authToken, isaReference).status shouldBe NO_CONTENT
  }

  Scenario("4. A reporting-window override only applies to the authenticated user") {
    assumeOverridesSupported()

    Given("Two users have different reporting-window overrides")
    val firstIsaReference  = generateRandomZReference()
    val secondIsaReference = generateRandomZReference()
    val firstAuthToken     = authHelper.getAuthBearerToken(firstIsaReference, uniqueCredentialId())
    val secondAuthToken    = authHelper.getAuthBearerToken(secondIsaReference, uniqueCredentialId())
    val now                = Instant.now()

    setOverride(firstAuthToken, firstIsaReference, now.plusSeconds(3600), now.plusSeconds(7200)).status shouldBe NO_CONTENT
    setOverride(secondAuthToken, secondIsaReference, now.minusSeconds(60), now.plusSeconds(300)).status shouldBe NO_CONTENT

    When("Each user submits a monthly return")
    val firstSubmission  = submissionRequest(firstAuthToken, firstIsaReference)
    val secondSubmission = submissionRequest(secondAuthToken, secondIsaReference)

    Then("Only the user with the active override can submit")
    firstSubmission.status shouldBe FORBIDDEN
    secondSubmission.status shouldBe NO_CONTENT
  }

  Scenario("5. An invalid reporting-window override does not replace an existing override") {
    assumeOverridesSupported()

    Given("I have configured an active reporting window")
    val isaReference = generateRandomZReference()
    val authToken    = authHelper.getAuthBearerToken(isaReference, uniqueCredentialId())
    val now          = Instant.now()
    setOverride(authToken, isaReference, now.minusSeconds(60), now.plusSeconds(300)).status shouldBe NO_CONTENT

    When("I try to replace it with an end date before its start date")
    val invalidResponse = setOverride(authToken, isaReference, now.plusSeconds(300), now.minusSeconds(60))

    Then("The invalid override is rejected")
    invalidResponse.status shouldBe BAD_REQUEST

    And("The previous active override still permits a monthly return")
    submissionRequest(authToken, isaReference).status shouldBe NO_CONTENT
  }

  private def setOverride(authToken: String, isaReference: String, startDate: Instant, endDate: Instant) =
    testSupportService.setReportingWindowOverride(
      isaReference,
      startDate,
      endDate,
      validHeadersOnlyWithToken(authToken)
    )

  private def uniqueCredentialId(): String = s"disa-api-tests-${UUID.randomUUID()}"

  private def assumeOverridesSupported(): Unit =
    assume(
      env.environment != "local",
      "Reporting-window overrides are only applied when disa-returns uses disa-returns-stubs"
    )
}
