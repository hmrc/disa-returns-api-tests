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
import play.api.libs.json.{JsArray, Json}
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.utils.BaseSpec

class ReportingSummarySpec extends BaseSpec, LazyLogging {

  Scenario(
    s"1. Verify 'Results Summary' API response gives status code 204 and able to see the 'state of the report results' from reconciliation"
  ) {
    Given("I Receive the summary from NPS and Save it on the database using the call back endpoint")
    val totalRecords                                  = 1000
    val month                                         = "AUG"
    val isaReference                                  = generateRandomZReference()
    val receivedSummaryResponse: StandaloneWSResponse =
      reportingService.makeReturnSummaryCallback(
        isaReference,
        taxYear,
        month,
        totalRecords,
        validHeadersOnlyWithToken
      )

    Then("I got the status code 204")
    receivedSummaryResponse.status shouldBe 204

    When("I request 'reporting results summary' via a GET request")
    val receivedReportingResultsSummaryResponse: StandaloneWSResponse =
      reportingService.getReportingResultsSummary(isaReference, taxYear, month, validHeadersOnlyWithToken)

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
    Given("I Receive the summary from NPS and Save it on the database using the test support API")
    val isaReference                                  = generateRandomZReference()
    val totalRecords                                  = Array(1, 2, 3)
    val month                                         = "AUG"
    val receivedSummaryResponse: StandaloneWSResponse =
      reportingService.triggerReportReadyScenario(
        isaReference,
        taxYear,
        month,
        totalRecords,
        validHeadersOnlyWithToken
      )

    Then("I got the status code 204")
    receivedSummaryResponse.status shouldBe 204

    When("I request 'reporting results summary' via a GET request")
    val receivedReportingResultsSummaryResponse: StandaloneWSResponse =
      reportingService.getReportingResultsSummary(isaReference, taxYear, month, validHeadersOnlyWithToken)

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
    val month                                                         = "APR"
    val isaReference                                                  = generateRandomZReference()
    When("I request 'reporting results summary' via a GET request when the report is not exists")
    val receivedReportingResultsSummaryResponse: StandaloneWSResponse =
      reportingService.getReportingResultsSummary(isaReference, taxYear, month, validHeadersOnlyWithToken)

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
    Given("A valid reconciliation report has been declared successfully via the declaration endpoint")
    val isaReference                                  = generateRandomZReference()
    val totalRecords                                  = Array(1, 2, 3)
    val month                                         = "AUG"
    val receivedSummaryResponse: StandaloneWSResponse =
      reportingService.triggerReportReadyScenario(
        isaReference,
        taxYear,
        month,
        totalRecords,
        validHeadersOnlyWithToken
      )

    Then("I got the status code 204 confirming the data was successfully stored")
    receivedSummaryResponse.status shouldBe 204

    When("I call the 'Reporting Results Endpoint' to retrieve the full report results from reconciliation")
    val checkResponse                                 =
      reportingService.getReportingResultsSummary(isaReference, taxYear, month, validHeadersOnlyWithToken)
    println(s"Summary check status: ${checkResponse.status}, body: ${checkResponse.body}")
    val receivedResultsResponse: StandaloneWSResponse =
      reportingService.getReportingResults(
        isaReference,
        taxYear,
        month,
        page = 0,
        validHeadersOnlyWithToken
      )

    Then("I should receive status code 200 OK")
    receivedResultsResponse.status shouldBe 200

    And("The response body should contain valid report data from reconciliation")
    val json = Json.parse(receivedResultsResponse.body)
    (json \ "currentPage").as[Int]   shouldEqual 0
    (json \ "recordsInPage").as[Int]      should be > 0
    (json \ "totalRecords").as[Int]       should be >= (json \ "recordsInPage").as[Int]
    (json \ "totalRecords").as[Int]  shouldEqual totalRecords.sum
    (json \ "totalNumberOfPages").as[Int] should be > 0

    And("Each return result should include valid account information and reconciliation details")
    val returnResults = (json \ "returnResults").as[JsArray]
    returnResults.value.nonEmpty shouldBe true

    val firstRecord = returnResults.value.head
    (firstRecord \ "accountNumber").asOpt[String].isDefined           shouldBe true
    (firstRecord \ "accountNumber").asOpt[String].nonEmpty            shouldBe true
    (firstRecord \ "nino").asOpt[String].isDefined                    shouldBe true
    (firstRecord \ "nino").asOpt[String].nonEmpty                     shouldBe true
    (firstRecord \ "issueIdentified" \ "code").asOpt[String].nonEmpty shouldBe true
  }
}
