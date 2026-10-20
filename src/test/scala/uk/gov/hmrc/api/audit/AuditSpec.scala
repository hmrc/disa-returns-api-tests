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

package uk.gov.hmrc.api.audit

import com.typesafe.scalalogging.LazyLogging
import play.api.http.Status.{BAD_REQUEST, FORBIDDEN, NO_CONTENT, OK, UNAUTHORIZED}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.api.utils.BaseSpec
import uk.gov.hmrc.api.utils.MockMonthlyReturnData.*
import uk.gov.hmrc.api.utils.RandomDataGenerator.{generateNino, generateSTDCode}

import java.time.Instant

/** Triggers the APIUploadStarted, APIValidation and APIDeclarationSubmitted audit events in disa-returns. The events
  * are not asserted here; they are checked in Splunk by searching on the Z-reference logged by each scenario.
  */
class AuditSpec extends BaseSpec, LazyLogging {

  private val submissionClockInstant = Instant.parse(s"${declarationPeriodDate}T00:00:00Z")

  // ============= APIUploadStarted ==========================================

  Scenario("1. First successful submission in the period triggers APIUploadStarted") {
    Given("I have a valid authentication and an ISA reference")
    val isaReference = generateRandomZReference()
    val authToken    = authHelper.getAuthBearerToken(isaReference)
    logExpectation(1, isaReference, "APIUploadStarted x1, APIValidation(Success) x1")

    When("I POST a valid submission request")
    val response = submissionRequest(authToken, isaReference)

    Then("A 204 status code is returned")
    response.status shouldBe NO_CONTENT
  }

  Scenario("2. Subsequent successful submissions in the period do not trigger APIUploadStarted again") {
    Given("I have a valid authentication and an ISA reference")
    val isaReference = generateRandomZReference()
    val authToken    = authHelper.getAuthBearerToken(isaReference)
    logExpectation(2, isaReference, "APIUploadStarted x1 only, APIValidation(Success) x2")

    When("I POST two valid submission requests")
    val firstResponse  = submissionRequest(authToken, isaReference)
    val secondResponse = submissionRequest(authToken, isaReference)

    Then("A 204 status code is returned for both")
    firstResponse.status  shouldBe NO_CONTENT
    secondResponse.status shouldBe NO_CONTENT
  }

  Scenario("3. A failed first submission does not trigger APIUploadStarted until a later submission succeeds") {
    Given("I have a valid authentication and an ISA reference")
    val isaReference = generateRandomZReference()
    val authToken    = authHelper.getAuthBearerToken(isaReference)
    logExpectation(
      3,
      isaReference,
      "request 1: APIValidation(Failure), no APIUploadStarted; request 2: APIUploadStarted x1, APIValidation(Success)"
    )

    When("I POST an invalid submission request")
    val invalidResponse = submissionRequest(authToken, isaReference, ndString = recordsMissingField(1, "firstName"))

    Then("A 400 status code is returned")
    invalidResponse.status shouldBe BAD_REQUEST

    When("I POST a valid submission request")
    val validResponse = submissionRequest(authToken, isaReference)

    Then("A 204 status code is returned")
    validResponse.status shouldBe NO_CONTENT
  }

  Scenario("4. A Z-reference mismatch does not trigger any audit events") {
    Given("I have a valid authentication for one ISA reference")
    val authorisedIsaReference = generateRandomZReference()
    val otherIsaReference      = generateRandomZReference()
    val authToken              = authHelper.getAuthBearerToken(authorisedIsaReference)
    logExpectation(4, otherIsaReference, s"no events (token issued for $authorisedIsaReference)")

    When("I POST a valid submission request for a different ISA reference")
    val response = submissionRequest(authToken, otherIsaReference)

    Then("A 401 status code is returned")
    response.status shouldBe UNAUTHORIZED
  }

  // ============= APIValidation ==========================================

  Scenario(
    "5. Second level validation errors across several records trigger APIValidation failure with error volumes"
  ) {
    Given("I have a valid authentication and an ISA reference")
    val isaReference = generateRandomZReference()
    val authToken    = authHelper.getAuthBearerToken(isaReference)
    logExpectation(5, isaReference, "APIValidation(Failure) with errorDetails volumes > 1, no APIUploadStarted")

    When("I POST a submission request where several records have validation errors")
    val ndString = recordsMissingField(3, "firstName") + recordsMissingField(2, "lastName") + validNdjsonTestData()
    val response = submissionRequest(authToken, isaReference, ndString = ndString)

    Then("A 400 status code is returned")
    response.status shouldBe BAD_REQUEST
  }

  Scenario("6. A record missing its NINO triggers APIValidation failure") {
    Given("I have a valid authentication and an ISA reference")
    val isaReference = generateRandomZReference()
    val authToken    = authHelper.getAuthBearerToken(isaReference)
    logExpectation(
      6,
      isaReference,
      "APIValidation(Failure) with errorType nino_or_acc_num_missing, no APIUploadStarted"
    )

    When("I POST a submission request with a record missing its NINO")
    val response = submissionRequest(authToken, isaReference, ndString = recordsMissingField(1, "nino"))

    Then("A 400 status code is returned")
    response.status shouldBe BAD_REQUEST
  }

  Scenario("7. A submission while the reporting window is closed does not trigger APIValidation") {
    Given("I have a valid authentication and an ISA reference with a closed reporting window")
    val isaReference = generateRandomZReference()
    val authToken    = authHelper.getAuthBearerToken(isaReference)
    closeReportingWindow(isaReference)
    logExpectation(7, isaReference, "no events")

    When("I POST a valid submission request")
    val response = submissionRequest(authToken, isaReference)

    Then("A 403 status code is returned")
    response.status shouldBe FORBIDDEN
  }

  // ============= APIDeclarationSubmitted ==========================================

  Scenario("8. A successful declaration after multiple submissions triggers APIDeclarationSubmitted success") {
    Given("I have a valid authentication and an ISA reference")
    val isaReference = generateRandomZReference()
    val authToken    = authHelper.getAuthBearerToken(isaReference)
    logExpectation(8, isaReference, "APIDeclarationSubmitted(Success) with numberOfRequests 2 and numberOfEntries 8")

    And("I have submitted monthly return data in two requests")
    submissionRequest(authToken, isaReference).status shouldBe NO_CONTENT
    submissionRequest(authToken, isaReference).status shouldBe NO_CONTENT

    When("I POST a declaration request")
    val response = declarationRequest(authToken, isaReference)

    Then("A 200 status code is returned")
    response.status shouldBe OK
  }

  Scenario("9. A successful nil return declaration triggers APIDeclarationSubmitted success") {
    Given("I have a valid authentication and an ISA reference")
    val isaReference = generateRandomZReference()
    val authToken    = authHelper.getAuthBearerToken(isaReference)
    logExpectation(9, isaReference, "APIDeclarationSubmitted(Success) with nilReturn yes")

    When("I POST a declaration request for a nil return")
    val response = declarationRequest(authToken, isaReference, nilReturn = true)

    Then("A 200 status code is returned")
    response.status shouldBe OK
  }

  Scenario("10. A declaration while the reporting window is closed triggers APIDeclarationSubmitted failure") {
    Given("I have a valid authentication and an ISA reference with a closed reporting window")
    val isaReference = generateRandomZReference()
    val authToken    = authHelper.getAuthBearerToken(isaReference)
    closeReportingWindow(isaReference)
    logExpectation(10, isaReference, "APIDeclarationSubmitted(Failure) for reporting window closed")

    When("I POST a declaration request")
    val response = declarationRequest(authToken, isaReference)

    Then("A 403 status code is returned")
    response.status shouldBe FORBIDDEN
  }

  private def logExpectation(scenario: Int, isaReference: String, expected: String): Unit =
    logger.info(s"[AUDIT] Scenario $scenario - zReference: $isaReference - expect: $expected")

  private def closeReportingWindow(isaReference: String): Unit =
    disaReturnsService
      .setReportingWindowOverride(
        isaReference,
        submissionClockInstant.plusSeconds(3600),
        submissionClockInstant.plusSeconds(7200),
        declarationPeriodDate
      )
      .status shouldBe NO_CONTENT

  private def recordsMissingField(count: Int, field: String): String =
    toNdjson(Seq.fill(count) {
      Json.toJson(getLISASubscriptionPayload(generateNino(), generateSTDCode())).as[JsObject] - field
    })
}
