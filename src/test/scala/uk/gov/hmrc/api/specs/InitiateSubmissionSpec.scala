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

package uk.gov.hmrc.api.specs

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.constant.*

class InitiateSubmissionSpec extends BaseSpec, LazyLogging {

  Scenario(
    s"1. Verify 'Initiate Returns Submission' API response gives status code 200 when obligation has not been met and reporting window is open"
  ) {
    Given("I set the reporting windows as open and when no obligation has met")
    disaReturnsStubService.setReportingWindow(true)
    disaReturnsStubService.setNoObligation(isaReferenceId)

    When("I POST a request 'Initiate Returns Submission' API")
    val initiateResponse: StandaloneWSResponse = postInitiateReturnsSubmission()

    Then("I got the status code 200")
    initiateResponse.status shouldBe 200
    val json = Json.parse(initiateResponse.body)
    (json \ "returnId").asOpt[String] should not be empty
    (json \ "action").asOpt[String]   should not be empty
    (json \ "boxId").asOpt[String]    should not be empty

  }

  Scenario(
    s"2. Verify 'Initiate Returns Submission' API response gives status code 403 when obligation has been met and reporting window is open"
  ) {
    Given("I set the reporting windows as open and when obligation has met")
    disaReturnsStubService.setReportingWindow(true)
    disaReturnsStubService.setObligationStatusTrue(isaReferenceId)

    When("I execute 'Initiate Returns Submission' API")
    val initiateResponse: StandaloneWSResponse = postInitiateReturnsSubmission(isaReferenceId)

    Then("I got the status code 403 with the correct obligation closed error response body")
    initiateResponse.status shouldBe 403
    val json = Json.parse(initiateResponse.body)
    (json \ "code").as[String]    shouldBe ObligationClosed.code
    (json \ "message").as[String] shouldBe ObligationClosed.message
  }

  Scenario(
    s"3. Verify 'Initiate Returns Submission' API response gives status code 403 when no obligation has met and reporting window is closed"
  ) {
    Given("I set the reporting windows as closed and when no obligation has met")
    disaReturnsStubService.setReportingWindow(false)
    disaReturnsStubService.setNoObligation(isaReferenceId)

    When("I execute 'Initiate Returns Submission' API")
    val initiateResponse: StandaloneWSResponse = postInitiateReturnsSubmission()

    Then("I got the status code 403 stating the reporting window is closed")
    initiateResponse.status shouldBe 403
    val json = Json.parse(initiateResponse.body)
    (json \ "code").as[String]    shouldBe ReportingWindowClosed.code
    (json \ "message").as[String] shouldBe ReportingWindowClosed.message
  }

  Scenario(
    s"4. Verify 'Initiate Returns Submission' API response gives status code 403 when obligation has been met and reporting window is closed"
  ) {
    Given("I set the reporting windows as closed and when obligation has been met")
    disaReturnsStubService.setReportingWindow(false)
    disaReturnsStubService.setObligationStatusTrue(isaReferenceId)

    When("I execute 'Initiate Returns Submission' API")
    val initiateResponse: StandaloneWSResponse = postInitiateReturnsSubmission(isaReferenceId)

    Then("I got the status code 403 stating that the obligation and reporting window is failed")
    initiateResponse.status shouldBe 403
    val json = Json.parse(initiateResponse.body)
    (json \ "code").as[String]    shouldBe Forbidden.code
    (json \ "message").as[String] shouldBe Forbidden.message
    val errors = (json \ "errors").as[Seq[JsValue]]
    errors.map(e => (e \ "code").as[String]).head    shouldBe ReportingWindowClosed.code
    errors.map(e => (e \ "message").as[String]).head shouldBe ReportingWindowClosed.message
    errors.map(e => (e \ "code").as[String])(1)      shouldBe ObligationClosed.code
    errors.map(e => (e \ "message").as[String])(1)   shouldBe ObligationClosed.message
  }

  Scenario(
    s"5. Verify 'Initiate Returns Submission' API response gives status code 500 for an internal server error correctly when etmp returns downstream error"
  ) {
    Given("I set the reporting windows as open and when no obligation has been met")
    disaReturnsStubService.setReportingWindow(true)
    disaReturnsStubService.setNoObligation(isaReferenceId)

    When("I execute 'Initiate Returns Submission' API")
    val initiateResponse: StandaloneWSResponse =
      postInitiateReturnsSubmission(isaManagerReference = "Z1234")

    Then("I got the status code 500 stating an internal server error")
    initiateResponse.status shouldBe 500
    val json = Json.parse(initiateResponse.body)
    (json \ "code").as[String]    shouldBe InternalServerError.code
    (json \ "message").as[String] shouldBe InternalServerError.message
  }

  Scenario(
    s"6. Verify 'Initiate Returns Submission' API response gives status code '400 - bad request' for an invalid payload (invalid totalRecords)"
  ) {
    Given("I set the reporting windows as open and when no obligation has been met")
    disaReturnsStubService.setReportingWindow(true)
    disaReturnsStubService.setNoObligation(isaReferenceId)

    When("I execute 'Initialise returns submission' api with an invalid no of totalRecords")
    val initiateResponse: StandaloneWSResponse = postInitiateReturnsSubmission(totalRecords = -1)

    Then("I got the status code 400 stating a bad request")
    initiateResponse.status shouldBe 400
    val json = Json.parse(initiateResponse.body)
    (json \ "code").as[String]    shouldBe BadRequest.code
    (json \ "message").as[String] shouldBe BadRequest.message
  }

  Scenario(
    s"8. Verify 'Initialise returns submission' api response gives status code '401 - invalid bearer token' error when an invalid bearer token used"
  ) {
    Given("I set the reporting windows as open and when no obligation has been met")
    disaReturnsStubService.setReportingWindow(true)
    disaReturnsStubService.setNoObligation(isaReferenceId)

    When("I execute 'Initialise returns submission' api with an invalid token")
    val initiateResponse: StandaloneWSResponse =
      postInitiateReturnsSubmission(headers = headersIncorrectBearerToken)

    Then("I got the status code 401 stating a bad request")
    initiateResponse.status shouldBe 401
    val json = Json.parse(initiateResponse.body)
    (json \ "code").as[String]    shouldBe InvalidBearerToken.code
    (json \ "message").as[String] shouldBe InvalidBearerToken.message
  }

  def postInitiateReturnsSubmission(
    isaManagerReference: String = isaReferenceId,
    headers: Map[String, String] = validHeaders,
    totalRecords: Int = 1000,
    taxYear: Int = currentYear,
    submissionPeriod: String = "APR"
  ): StandaloneWSResponse =
    initialiseReturnsSubmissionService.postInitialiseReturnsSubmissionApi(
      totalRecords = totalRecords,
      submissionPeriod = submissionPeriod,
      taxYear = taxYear,
      isManagerReference = isaManagerReference,
      headers = headers
    )
}
