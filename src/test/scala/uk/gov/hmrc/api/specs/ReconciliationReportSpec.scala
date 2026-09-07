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

import org.scalactic.Prettifier.default
import play.api.http.Status.{NO_CONTENT, OK}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.utils.BaseSpec

class ReconciliationReportSpec extends BaseSpec {

  Scenario(
    s"1. Verify 'Results Endpoint' returns status code 200 OK after successful reconciliation declaration"
  ) {
    Given("I have a valid authentication and an ISA reference")
    val isaReference      = generateRandomZReference()
    val authToken: String = authHelper.getAuthBearerToken(isaReference)

    Given("I receive a reconciliation report ready callback using the test support API")
    val reportReadyCallbackResponse: StandaloneWSResponse =
      testSupportService.triggerGenerateReport(
        isaReference,
        totalRecords,
        validHeadersOnlyWithToken(authToken)
      )

    Then("I got the status code 204 confirming the data was successfully stored")
    println(Console.GREEN + reportReadyCallbackResponse.body + Console.RESET)
    reportReadyCallbackResponse.status shouldBe NO_CONTENT

    When("I request 'Reporting Results Endpoint' via a GET request to retrieve the full reconciliation report")
    val receivedReportingResultsEndpointResponse: StandaloneWSResponse =
      disaReturnsService.getReconciliationReport(
        isaReference,
        page = 0,
        validHeadersOnlyWithToken(authToken)
      )

    Then("I should receive status code 200 OK")
    receivedReportingResultsEndpointResponse.status shouldBe OK

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
