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
import uk.gov.hmrc.api.constant.MismatchRecordCount
import uk.gov.hmrc.api.utils.MockMonthlyReturnData.validNdjsonTestData

class CompleteMonthlyReturnsSpec extends BaseSpec, LazyLogging {
  Scenario(
    s"1. Verify 'Complete Monthly Returns' API response gives status code 200 for a valid Monthly Returns submission"
  ) {
    Given("I set the reporting windows as open and when no obligation has met")
    disaReturnsStubService.setReportingWindow(true)
    disaReturnsStubService.setNoObligation(isaReferenceId)

    When("I POST a request 'Initiate Returns Submission' API to get a returnId")
    val initiateResponse = postInitiateReturnsSubmission()
    initiateResponse.status shouldBe 200
    val initiateResponseJson = Json.parse(initiateResponse.body)
    val returnId             = (initiateResponseJson \ "returnId").as[String]

    When("I POST a request 'Monthly Returns Submission' API")
    val monthlyReturnsSubmissionResponse =
      postMonthlyReturnsSubmission(returnId = returnId)

    Then("I got the status code 204")
    monthlyReturnsSubmissionResponse.status shouldBe 204

    When("I POST a second submission request to 'Monthly Returns Submission' API")
    val monthlyReturnsSubmissionResponse2 =
      postMonthlyReturnsSubmission(returnId = returnId)

    Then("I got the status code 204")
    monthlyReturnsSubmissionResponse2.status shouldBe 204

    When("I POST the 'Complete Monthly Returns' API")
    val completeMonthlyReturnsResponse =
      postCompleteMonthlyReturns(returnId = returnId)

    Then("I got the status code 204")
    completeMonthlyReturnsResponse.status shouldBe 200

  }

  Scenario(
    s"2. Verify 'Complete Monthly Returns' API response gives status code 400 for an incorrect 'Initiate Returns' totalRecords count"
  ) {
    Given("I set the reporting windows as open and when no obligation has met")
    disaReturnsStubService.setReportingWindow(true)
    disaReturnsStubService.setNoObligation(isaReferenceId)

    When("I POST a request 'Initiate Returns Submission' API to get a returnId")
    val initiateResponse = postInitiateReturnsSubmission()
    initiateResponse.status shouldBe 200
    val initiateResponseJson = Json.parse(initiateResponse.body)
    val returnId             = (initiateResponseJson \ "returnId").as[String]

    When("I POST a request 'Monthly Returns Submission' API")
    val monthlyReturnsSubmissionResponse =
      postMonthlyReturnsSubmission(returnId = returnId)

    Then("I got the status code 204")
    monthlyReturnsSubmissionResponse.status shouldBe 204

    When("I POST the 'Complete Monthly Returns' API")
    val completeMonthlyReturnsResponse =
      postCompleteMonthlyReturns(returnId = returnId)

    Then("I got the status code 400 & correct error response body")
    completeMonthlyReturnsResponse.status shouldBe 400
    val responseBody = Json.parse(completeMonthlyReturnsResponse.body)
    (responseBody \ "code").as[String]    shouldBe MismatchRecordCount.code
    (responseBody \ "message").as[String] shouldBe MismatchRecordCount.message
  }

  Scenario(
    s"3. Verify 'Complete Monthly Returns' API response gives status code 403 when when user tries to resend the same 'Complete Monthly Returns' API"
  ) {
    Given("I set the reporting windows as open and when obligation has not met")
    disaReturnsStubService.setReportingWindow(true)
    disaReturnsStubService.setNoObligation(isaReferenceId)

    When("I POST a request 'Initiate Returns Submission' API to get a returnId")
    val initiateResponse = postInitiateReturnsSubmission()
    initiateResponse.status shouldBe 200
    val initiateResponseJson = Json.parse(initiateResponse.body)
    val returnId             = (initiateResponseJson \ "returnId").as[String]

    When("I POST a request 'Monthly Returns Submission' API")
    val monthlyReturnsSubmissionResponse =
      postMonthlyReturnsSubmission(returnId = returnId)

    Then("I got the status code 204")
    monthlyReturnsSubmissionResponse.status shouldBe 204

    When("I POST a second submission request to 'Monthly Returns Submission' API")
    val monthlyReturnsSubmissionResponse2 =
      postMonthlyReturnsSubmission(returnId = returnId)

    Then("I got the status code 204")
    monthlyReturnsSubmissionResponse2.status shouldBe 204

    When("I POST the 'Complete Monthly Returns' API")
    val completeMonthlyReturnsResponse =
      postCompleteMonthlyReturns(returnId = returnId)

    Then("I got the status code 204")
    completeMonthlyReturnsResponse.status shouldBe 200

    When("I POST the same 'Complete Monthly Returns' API for the second time")
    val completeMonthlyReturnsResponse2 =
      postCompleteMonthlyReturns(returnId = returnId)

    Then("I got the status code 204")
    completeMonthlyReturnsResponse2.status shouldBe 403

  }

  Scenario(
    s"4. Verify 'Complete Monthly Returns' API response gives status code 401 for an authentication failure"
  ) {
    Given("I set the reporting windows as open and when no obligation has met")
    disaReturnsStubService.setReportingWindow(true)
    disaReturnsStubService.setNoObligation(isaReferenceId)

    When("I POST a request 'Initiate Returns Submission' API to get a returnId")
    val initiateResponse = postInitiateReturnsSubmission()
    initiateResponse.status shouldBe 200
    val initiateResponseJson = Json.parse(initiateResponse.body)
    val returnId             = (initiateResponseJson \ "returnId").as[String]

    When("I POST a request 'Monthly Returns Submission' API")
    val monthlyReturnsSubmissionResponse =
      postMonthlyReturnsSubmission(returnId = returnId)

    Then("I got the status code 204")
    monthlyReturnsSubmissionResponse.status shouldBe 204

    When("I POST a second submission request to 'Monthly Returns Submission' API")
    val monthlyReturnsSubmissionResponse2 =
      postMonthlyReturnsSubmission(returnId = returnId)

    Then("I got the status code 204")
    monthlyReturnsSubmissionResponse2.status shouldBe 204

    When("I POST the 'Complete Monthly Returns' API")
    val completeMonthlyReturnsResponse =
      postCompleteMonthlyReturnsWithoutAuthentication(returnId = returnId)

    Then("I got the status code 401")
    completeMonthlyReturnsResponse.status shouldBe 401

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
      totalRecords = 12,
      submissionPeriod = "APR",
      taxYear = currentYear,
      isManagerReference = isaManagerReference,
      headers = headers
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

  def postCompleteMonthlyReturnsWithoutAuthentication(
    isaManagerReference: String = isaReferenceId,
    headers: Map[String, String] = validHeadersWithInvalidToken,
    returnId: String
  ): StandaloneWSResponse =
    completeMonthlyReturnsService.postCompleteMonthlyReturns(
      isaManagerReference = isaManagerReference,
      returnId = returnId,
      headers = headers
    )
}
