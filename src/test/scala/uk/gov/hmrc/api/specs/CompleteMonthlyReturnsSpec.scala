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
import uk.gov.hmrc.api.utils.MockMonthlyReturnData.validNdjsonTestData

class CompleteMonthlyReturnsSpec extends BaseSpec, LazyLogging {

  Scenario(
    s"1. Verify that the API successfully locks further submissions for a given isaManagerReferenceNumber and returnId"
  ) {
    Given("I set the reporting windows as open")
    disaReturnsStubService.setReportingWindow(true)

    When("I POST a request 'Open Obligation Window")
    openObligationWindowService.setOpenObligationWindow()

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

    When("I POST a second submission request to 'Complete Monthly Returns' API")
    val footerReturnsSubmissionResponse: StandaloneWSResponse =
      postCompleteMonthlyReturnsSubmission(returnId = returnId)

    Then("I got the status code 400")
    footerReturnsSubmissionResponse.status shouldBe 400
  }

  Scenario(
    s"2. Verify that after sending /complete, any further /submission requests for the same isaManagerReferenceNumber and returnId are rejected"
  ) {
    Given("I set the reporting windows as open")
    disaReturnsStubService.setReportingWindow(true)

    When("I POST a request 'Open Obligation Window")
    openObligationWindowService.setOpenObligationWindow()

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

    When("I POST a submission request to 'Footer' API")
    val footerReturnsSubmissionResponse: StandaloneWSResponse =
      postCompleteMonthlyReturnsSubmission(returnId = returnId)

    When("I POST a second submission request to 'Complete Monthly Returns' API")
    val footerReturnsSubmissionResponse2: StandaloneWSResponse =
      postCompleteMonthlyReturnsSubmission(returnId = returnId)

    Then("I got the status code 400")
    footerReturnsSubmissionResponse2.status shouldBe 400
  }

  Scenario(
    s"3. Verify that the API successfully completion of monthly returns or a given valid isaManagerReferenceNumber and returnId"
  ) {
    Given("I set the reporting windows as open")
    disaReturnsStubService.setReportingWindow(true)

    When("I POST a request 'Open Obligation Window")
    openObligationWindowService.setOpenObligationWindow()

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

    When("I POST a second submission request to 'Footer' API")
    val footerReturnsSubmissionResponse: StandaloneWSResponse =
      postCompleteMonthlyReturnsSubmission(returnId = returnId)

    Then("I got the status code 200")
    footerReturnsSubmissionResponse.status shouldBe 200
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
      totalRecords = 6,
      submissionPeriod = "APR",
      taxYear = currentYear,
      isManagerReference = isaManagerReference,
      headers = headers
    )

  def postCompleteMonthlyReturnsSubmission(
    isaManagerReference: String = isaReferenceId,
    returnId: String,
    headers: Map[String, String] = headersMapWithoutClientIDButValidTokenWithoutContentType
  ): StandaloneWSResponse =
    footerReturnsSubmissionService.postCompleteMonthlyReturnsSubmission(
      isaManagerReference = isaManagerReference,
      returnId = returnId,
      headers = headers
    )
}
