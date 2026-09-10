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
import play.api.libs.json.{JsObject, JsValue, Json}
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

    When("I follow each cursor from the results endpoint")
    val limit                                     = 2
    var cursor: Option[String]                    = None
    var returnResults                             = Seq.empty[JsValue]
    var receivedReportingResultsEndpointResponses = Seq.empty[StandaloneWSResponse]
    var moreResultsAvailable                      = true

    while (moreResultsAvailable) {
      val response = disaReturnsService.getReconciliationReport(
        isaReference,
        validHeadersOnlyWithToken(authToken),
        cursor = cursor,
        limit = Some(limit)
      )
      val json     = Json.parse(response.body).as[JsObject]

      receivedReportingResultsEndpointResponses :+= response
      returnResults ++= (json \ "returnResults").as[Seq[JsValue]]

      (json \ "currentPage").toOption        shouldBe None
      (json \ "recordsInPage").toOption      shouldBe None
      (json \ "totalRecords").toOption       shouldBe None
      (json \ "totalNumberOfPages").toOption shouldBe None

      cursor = (json \ "nextCursor").toOption.map(_.as[String])
      cursor.foreach(_.nonEmpty shouldBe true)
      moreResultsAvailable = cursor.nonEmpty
    }

    Then("each request should receive status code 200 OK")
    all(receivedReportingResultsEndpointResponses.map(_.status)) shouldBe OK

    And("all reconciliation results should be returned across the cursor pages")
    returnResults.size shouldEqual totalRecords.sum
  }
}
