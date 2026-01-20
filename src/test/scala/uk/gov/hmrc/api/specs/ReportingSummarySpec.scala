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
import org.scalactic.Prettifier.default
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.utils.BaseSpec

class ReportingSummarySpec extends BaseSpec, LazyLogging {

  Scenario(
    s"1. Verify 'Results Summary' API response gives status code 204 and able to see the 'state of the report results' from reconciliation"
  ) {
    Given("I have a valid authentication and an ISA reference")
    val isaReference      = generateRandomZReference()
    val authToken: String = authHelper.getAuthBearerToken(isaReference)

    Given("I simulate a summary from NPS via callback endpoint")
    val totalRecords                                  = 1000
    val receivedSummaryResponse: StandaloneWSResponse =
      reportingService.makeReturnSummaryCallback(
        isaReference,
        taxYear,
        month,
        totalRecords,
        validHeadersOnlyWithToken(authToken)
      )

    Then("I got the status code 204")
    receivedSummaryResponse.status shouldBe 204

    When("I request 'reporting results summary' via a GET request")
    val receivedReportingResultsSummaryResponse: StandaloneWSResponse =
      reportingService.getReportingResultsSummary(
        isaReference,
        taxYear,
        month = month,
        validHeadersOnlyWithToken(authToken)
      )

    Then("I got the status code 200")
    receivedReportingResultsSummaryResponse.status shouldBe 200

    val json = Json.parse(receivedReportingResultsSummaryResponse.body)

    (json \ "returnResultsLocation").as[String] should include(
      s"/monthly/$isaReference/$taxYear/$month/results?page=0"
    )
    (json \ "totalRecords").as[Int]        shouldEqual totalRecords
    (json \ "numberOfPages").as[Int]            should be > 0
  }

  Scenario(
    s"2. Verify 'Results Summary' API response gives status code 204 and able to see the 'state of the report results' from reconciliation"
  ) {
    Given("I have a valid authentication and an ISA reference")
    val isaReference      = generateRandomZReference()
    val authToken: String = authHelper.getAuthBearerToken(isaReference)

    Given("I Receive the summary from NPS and Save it on the database using the test support API")
    val receivedSummaryResponse: StandaloneWSResponse =
      reportingService.triggerReportReadyScenario(
        isaReference,
        taxYear,
        month,
        totalRecords,
        validHeadersOnlyWithToken(authToken)
      )

    Then("I got the status code 204")
    receivedSummaryResponse.status shouldBe 204

    When("I request 'reporting results summary' via a GET request")
    val receivedReportingResultsSummaryResponse: StandaloneWSResponse =
      reportingService.getReportingResultsSummary(
        isaReference,
        taxYear,
        month = month,
        validHeadersOnlyWithToken(authToken)
      )

    Then("I got the status code 200")
    receivedReportingResultsSummaryResponse.status shouldBe 200

    val json = Json.parse(receivedReportingResultsSummaryResponse.body)

    (json \ "returnResultsLocation").as[String] should include(
      s"/monthly/$isaReference/$taxYear/$month/results?page=0"
    )
    (json \ "totalRecords").as[Int]        shouldEqual totalRecords.sum
    (json \ "numberOfPages").as[Int]            should be > 0
  }

  Scenario(
    s"3. Verify 'Results Summary' API response gives status code 404 NOT FOUND when the report results from reconciliation is not available"
  ) {
    Given("I have a valid authentication and an ISA reference")
    val isaReference      = generateRandomZReference()
    val authToken: String = authHelper.getAuthBearerToken(isaReference)

    When("I request 'reporting results summary' via a GET request when the report is not exists")
    val receivedReportingResultsSummaryResponse: StandaloneWSResponse =
      reportingService.getReportingResultsSummary(
        isaReference,
        taxYear,
        month = month,
        validHeadersOnlyWithToken(authToken)
      )

    Then("I got the status code 404")
    receivedReportingResultsSummaryResponse.status shouldBe 404

    val json = Json.parse(receivedReportingResultsSummaryResponse.body)

    (json \ "message").as[String] should include(
      s"No return found for $isaReference for $month $taxYear"
    )
  }

  Scenario(
    s"4. Verify 'Results Endpoint' returns status code 200 OK after successful reconciliation declaration"
  ) {
    Given("I have a valid authentication and an ISA reference")
    val isaReference      = generateRandomZReference()
    val authToken: String = authHelper.getAuthBearerToken(isaReference)

    Given("I Receive the summary from NPS and Save it on the database using the test support API")
    val npsReceivedSummaryResponse: StandaloneWSResponse =
      reportingService.triggerReportReadyScenario(
        isaReference,
        taxYear,
        month = month,
        totalRecords,
        validHeadersOnlyWithToken(authToken)
      )

    Then("I got the status code 204 confirming the data was successfully stored")
    npsReceivedSummaryResponse.status shouldBe 204

    When("I request 'Reporting Results Endpoint' via a GET request to retrieve the full reconciliation report")
    val receivedReportingResultsEndpointResponse: StandaloneWSResponse =
      reportingService.getReconcilationReportPage(
        isaReference,
        taxYear,
        month,
        page = 0,
        validHeadersOnlyWithToken(authToken)
      )

    Then("I should receive status code 200 OK")
    receivedReportingResultsEndpointResponse.status shouldBe 200

    And("The response body should contain valid report data from reconciliation")
    val json = Json.parse(receivedReportingResultsEndpointResponse.body)
    (json \ "currentPage").as[Int]   shouldEqual 0
    (json \ "recordsInPage").as[Int] shouldEqual 6
    (json \ "totalRecords").as[Int]       should be >= (json \ "recordsInPage").as[Int]
    (json \ "totalRecords").as[Int]  shouldEqual totalRecords.sum
    (json \ "totalNumberOfPages").as[Int] should be > 0

    And("The number of records in 'returnResults' should match 'recordsInPage'")
    val recordsInPage = (json \ "recordsInPage").as[Int]
    val returnResults = (json \ "returnResults").as[Seq[JsValue]]
    returnResults.size shouldEqual recordsInPage
  }
}
