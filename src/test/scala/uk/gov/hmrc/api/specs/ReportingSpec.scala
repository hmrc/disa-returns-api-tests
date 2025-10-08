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
import play.api.libs.json.Json
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.utils.BaseSpec

class ReportingSpec extends BaseSpec, LazyLogging {

  Scenario(
    s"1. Verify 'Results Summary' API response gives status code 204 and able to see the 'state of the report results' from reconciliation"
  ) {
    Given("I Receive the summary from NPS and Save it on the database using the call back endpoint")
    val receivedSummaryResponse: StandaloneWSResponse =
      reportingService.receiveSummaryAndSaveUsingCallbackApi(
        isaReferenceId,
        "2025-26",
        "FEB",
        validHeadersOnlyWithToken
      )

    Then("I got the status code 204")
    receivedSummaryResponse.status shouldBe 204

    When("I request 'reporting results summary' via a GET request")
    val receivedReportingResultsSummaryResponse: StandaloneWSResponse =
      reportingService.getReportingResultsSummary(isaReferenceId, "2025-26", "FEB", validHeadersOnlyWithToken)

    Then("I got the status code 204")
    receivedReportingResultsSummaryResponse.status shouldBe 200

    val json = Json.parse(receivedReportingResultsSummaryResponse.body)

    (json \ "returnResultsLocation").asOpt[String] should not be empty
    (json \ "totalRecords").asOpt[Int]             should not be empty
    (json \ "numberOfPages").asOpt[Int]            should not be empty
  }

  Scenario(
    s"2. Verify 'Results Summary' API response gives status code 204 and able to see the 'state of the report results' from reconciliation"
  ) {
    Given("I Receive the summary from NPS and Save it on the database using the test support API")
    val receivedSummaryResponse: StandaloneWSResponse =
      reportingService.receiveSummaryAndSaveUsingTestSupportApi(
        isaReferenceId,
        "2025-26",
        "AUG",
        validHeadersOnlyWithToken
      )

    Then("I got the status code 204")
    receivedSummaryResponse.status shouldBe 204

    When("I request 'reporting results summary' via a GET request")
    val receivedReportingResultsSummaryResponse: StandaloneWSResponse =
      reportingService.getReportingResultsSummary(isaReferenceId, "2025-26", "AUG", validHeadersOnlyWithToken)

    Then("I got the status code 204")
    receivedReportingResultsSummaryResponse.status shouldBe 200

    val json = Json.parse(receivedReportingResultsSummaryResponse.body)

    (json \ "returnResultsLocation").asOpt[String] should not be empty
    (json \ "totalRecords").asOpt[Int]             should not be empty
    (json \ "numberOfPages").asOpt[Int]            should not be empty
  }

  Scenario(
    s"3. Verify 'Results Summary' API response gives status code 404 NOT FOUND when the report results from reconciliation is not available"
  ) {
    val period                                                        = "2025-26"
    val month                                                         = "APR"
    When("I request 'reporting results summary' via a GET request when the report is not exists")
    val receivedReportingResultsSummaryResponse: StandaloneWSResponse =
      reportingService.getReportingResultsSummary(isaReferenceId, period, month, validHeadersOnlyWithToken)

    Then("I got the status code 404")
    receivedReportingResultsSummaryResponse.status shouldBe 404

    val json = Json.parse(receivedReportingResultsSummaryResponse.body)

    (json \ "message").asOpt[String] should contain(
      "No return found for " + isaReferenceId + " for " + month + " " + period
    )
  }
}
