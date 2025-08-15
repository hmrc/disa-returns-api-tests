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
import uk.gov.hmrc.api.utils.MockMonthlyReturnData.validNdjsonTestData

class MonthlyReturnsSubmissionSpec extends BaseSpec, LazyLogging {

  Scenario(
    s"1. Verify 'Monthly Returns Submission' API response gives status code 204 for a valid monthly report submission"
  ) {
    Given("I set the reporting windows as open")
    disaReturnsStubHelper.setReportingWindow(true)

    When("I POST a request 'Initiate Returns Submission' API to get a returnId")
    val initiateResponse: StandaloneWSResponse = postInitiateReturnsSubmission()
    initiateResponse.status shouldBe 200
    val initiateResponseJson = Json.parse(initiateResponse.body)
    val returnId             = (initiateResponseJson \ "returnId").as[String]

    When("I POST a request 'Monthly Returns Submission' API")
    val monthlyReturnsSubmissionResponse: StandaloneWSResponse =
      postMonthlyReturnsSubmission(returnId = returnId)

    Then("I got the status code 204")
    monthlyReturnsSubmissionResponse.status shouldBe 204

    When("I POST a second submission request to 'Monthly Returns Submission' API")
    val monthlyReturnsSubmissionResponse2: StandaloneWSResponse =
      postMonthlyReturnsSubmission(returnId = returnId)

    Then("I got the status code 204")
    monthlyReturnsSubmissionResponse2.status shouldBe 204
  }

  Scenario(
    s"2. Verify 'Monthly Returns Submission' API response gives status code 403 FORBIDDEN when reporting window is closed"
  ) {
    Given("I set the reporting windows as closed successfully")
    disaReturnsStubHelper.setReportingWindow(true)

    When("I POST a request 'Initiate Returns Submission' API to get a returnId")
    val initiateResponse: StandaloneWSResponse = postInitiateReturnsSubmission()
    initiateResponse.status shouldBe 200

    val initiateResponseJson = Json.parse(initiateResponse.body)
    val returnId             = (initiateResponseJson \ "returnId").as[String]

    Given("I set the reporting windows as closed")
    disaReturnsStubHelper.setReportingWindow(false)

    When("I POST a request 'Monthly Returns Submission' API")
    val monthlyReturnsSubmissionResponse: StandaloneWSResponse =
      postMonthlyReturnsSubmission(returnId = returnId)

    Then("I got the status code 403 & correct error response body")
    monthlyReturnsSubmissionResponse.status shouldBe 403
    val submitResponseJson = Json.parse(monthlyReturnsSubmissionResponse.body)
    (submitResponseJson \ "code").as[String]    shouldBe ReportingWindowClosed.code
    (submitResponseJson \ "message").as[String] shouldBe ReportingWindowClosed.message

  }

  Scenario(
    s"3. Verify 'Initialise returns submission' api response gives status code '400 - BAD_REQUEST' for an invalid payload (totalRecords negative number)"
  ) {
    Given("I set the reporting windows as open")
    disaReturnsStubHelper.setReportingWindow(true)

    val initiateResponse: StandaloneWSResponse = postInitiateReturnsSubmission()
    initiateResponse.status shouldBe 200
    val initiateResponseJson = Json.parse(initiateResponse.body)
    val returnId             = (initiateResponseJson \ "returnId").as[String]

    When("I POST a request 'Monthly Returns Submission' API")
    val monthlyReturnsSubmissionResponse: StandaloneWSResponse =
      postMonthlyReturnsSubmission(returnId = returnId, ndString = "")

    Then("I got the status code 400 BAD_REQUEST & correct error response body")
    monthlyReturnsSubmissionResponse.status shouldBe 400

    val submitResponseJson = Json.parse(monthlyReturnsSubmissionResponse.body)
    (submitResponseJson \ "code").as[String]    shouldBe InvalidNdJsonPayload.code
    (submitResponseJson \ "message").as[String] shouldBe InvalidNdJsonPayload.message
  }

  Scenario(
    s"4. Verify 'Monthly Returns Submission' API response gives status code '401 - UNAUTHORISED' error when an invalid bearer token is provided"
  ) {
    Given("I set the reporting windows as open")
    disaReturnsStubHelper.setReportingWindow(true)

    When("I POST a request 'Initiate Returns Submission' API to get a returnId")
    val initiateResponse: StandaloneWSResponse = postInitiateReturnsSubmission()
    initiateResponse.status shouldBe 200
    val initiateResponseJson = Json.parse(initiateResponse.body)
    val returnId             = (initiateResponseJson \ "returnId").as[String]

    When("I POST a request 'Monthly Returns Submission' API")
    val monthlyReturnsSubmissionResponse: StandaloneWSResponse =
      postMonthlyReturnsSubmission(returnId = returnId, headers = headersMapWithIncorrectClientIdAndIncorrectToken)

    Then("I got the status code 401 & correct error response body")
    monthlyReturnsSubmissionResponse.status shouldBe 401
    val submitResponseJson = Json.parse(monthlyReturnsSubmissionResponse.body)
    (submitResponseJson \ "code").as[String]    shouldBe InvalidBearerToken.code
    (submitResponseJson \ "message").as[String] shouldBe InvalidBearerToken.message
  }

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

  def postInitiateReturnsSubmission(
    isaManagerReference: String = isaReferenceId,
    headers: Map[String, String] = validHeaders
  ): StandaloneWSResponse =
    initialiseReturnsSubmissionService.postInitialiseReturnsSubmissionApi(
      totalRecords = 1000,
      submissionPeriod = "APR",
      taxYear = currentYear,
      isManagerReference = isaManagerReference,
      headers = headers
    )
}
