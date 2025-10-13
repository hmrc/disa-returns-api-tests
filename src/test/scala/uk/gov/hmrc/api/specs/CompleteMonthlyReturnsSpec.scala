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
import uk.gov.hmrc.api.utils.BaseSpec

class CompleteMonthlyReturnsSpec extends BaseSpec, LazyLogging {

  Scenario(
    s"1. Verify 'Complete Monthly Returns' API response gives status code 200 for a valid complete monthly returns submission"
  ) {
    Given("I set the reporting windows as open and when no obligation has met")
    val isaReference = isaReferenceId
    disaReturnsStubService.setReportingWindow(true)
    disaReturnsStubService.openObligationStatus(isaReference)

    Given("I created the client application and notification box")
    createClientApplication()
    createNotificationBoxAndSubscribe()

    When("I POST a request 'Initiate Returns Submission' API to get a returnId for 12 totalRecords")
    val taxYear                                = "2025-26"
    val month                                  = "AUG"
    val initiateResponse: StandaloneWSResponse = postInitiateReturnsSubmission(isaReference, month = month)
    initiateResponse.status shouldBe 200
    val initiateResponseJson = Json.parse(initiateResponse.body)
    val returnId             = (initiateResponseJson \ "returnId").as[String]

    When("I POST a request 'Monthly Returns Submission' API for 6 totalRecords for the first time")
    val monthlyReturnsSubmissionResponse =
      postMonthlyReturnsSubmission(returnId = returnId)

    Then("I got the status code 204")
    monthlyReturnsSubmissionResponse.status shouldBe 204

    When("I POST a second submission request to 'Monthly Returns Submission' API for the rest of 6 totalRecords")
    val monthlyReturnsSubmissionResponse2 =
      postMonthlyReturnsSubmission(returnId = returnId)

    Then("I got the status code 204")
    monthlyReturnsSubmissionResponse2.status shouldBe 204

    When("I POST the 'Complete Monthly Returns' API")
    val completeMonthlyReturnsResponse =
      postCompleteMonthlyReturns(taxYear = taxYear, month = month)

    Then("I got the status code 204")
    completeMonthlyReturnsResponse.status shouldBe 200
  }

  Scenario(
    s"2. Verify 'Complete Monthly Returns' API response gives status code 403 Obligation closed when the user tries to resend the same 'Complete Monthly Returns' API"
  ) {
    Given("I set the reporting windows as open and when obligation has not met")
    val isaReference = isaReferenceId
    disaReturnsStubService.setReportingWindow(true)
    disaReturnsStubService.openObligationStatus(isaReference)

    Given("I created the client application and notification box")
    createClientApplication()
    createNotificationBoxAndSubscribe()

    When("I POST a request 'Initiate Returns Submission' API to get a returnId")
    val taxYear                                = "2025-26"
    val month                                  = "JAN"
    val initiateResponse: StandaloneWSResponse = postInitiateReturnsSubmission(isaReference, month = month)
    initiateResponse.status shouldBe 200
    val initiateResponseJson: JsValue = Json.parse(initiateResponse.body)
    val returnId                      = (initiateResponseJson \ "returnId").as[String]

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
      postCompleteMonthlyReturns(taxYear = taxYear, month = month)

    Then("I got the status code 204")
    completeMonthlyReturnsResponse.status shouldBe 200

    When("I POST the same 'Complete Monthly Returns' API for the second time")
    val completeMonthlyReturnsResponse2 =
      postCompleteMonthlyReturns(taxYear = taxYear, month = month)

    Then("I got the status code 204")
    completeMonthlyReturnsResponse2.status shouldBe 403

  }

  Scenario(
    s"3. Verify 'Complete Monthly Returns' API response gives status code 401 for an authentication failure"
  ) {
    Given("I set the reporting windows as open and when no obligation has met")
    val isaReference = isaReferenceId
    disaReturnsStubService.setReportingWindow(true)
    disaReturnsStubService.openObligationStatus(isaReference)

    Given("I created the client application and notification box")
    createClientApplication()
    createNotificationBoxAndSubscribe()

    When("I POST a request 'Initiate Returns Submission' API to get a returnId")
    val taxYear          = "2025-26"
    val month            = "JAN"
    val initiateResponse = postInitiateReturnsSubmission(isaReference, month = month)
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
      postCompleteMonthlyReturns(taxYear = taxYear, month = month, headers = Map.empty)

    Then("I got the status code 401")
    completeMonthlyReturnsResponse.status shouldBe 401

  }
}
