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
    val reportReadyCallbackResponse: StandaloneWSResponse = generateReport(isaReference, authToken)

    Then("I got the status code 204 confirming the data was successfully stored")
    reportReadyCallbackResponse.status shouldBe NO_CONTENT

    When("I follow each cursor from the results endpoint")
    val (responses, returnResults) = getAllResults(isaReference, authToken)

    Then("each request should receive status code 200 OK")
    all(responses.map(_.status)) shouldBe OK

    And("all reconciliation results should be returned across the cursor pages")
    returnResults.size shouldEqual totalRecords.sum
  }

  Scenario(
    s"2. Verify 'Results Endpoint' allows an agent with a delegated enrolment for the ISA reference"
  ) {
    Given("A reconciliation report has been generated for an ISA reference")
    val isaReference = generateRandomZReference()
    generateReport(isaReference, authHelper.getAuthBearerToken(isaReference)).status shouldBe NO_CONTENT

    And("I am an agent with a delegated enrolment for the ISA reference")
    val agentToken = agentAuthBearerToken(isaReference)

    When("I GET the reconciliation report as the agent")
    val response = disaReturnsService.getReconciliationReport(isaReference, validHeadersOnlyWithToken(agentToken))

    Then("A 200 status code is returned")
    response.status shouldBe OK

    And("all reconciliation results are returned to the agent")
    val (_, returnResults) = getAllResults(isaReference, agentToken)
    returnResults.size shouldEqual totalRecords.sum
  }

  Scenario(
    s"3. Verify 'Results Endpoint' rejects an agent without a delegated enrolment for the ISA reference"
  ) {
    Given("A reconciliation report has been generated for an ISA reference")
    val isaReference = generateRandomZReference()
    generateReport(isaReference, authHelper.getAuthBearerToken(isaReference)).status shouldBe NO_CONTENT

    And("I am an agent with a delegated enrolment for a different ISA reference")
    val agentToken = agentAuthBearerToken(ZReferenceGenerator.generate())

    When("I GET the reconciliation report as the agent")
    val response = disaReturnsService.getReconciliationReport(isaReference, validHeadersOnlyWithToken(agentToken))

    assertAgentClientRelationshipNotFound(response)
  }

  Scenario(
    s"4. Verify the test support API allows an agent with a delegated enrolment to generate a reconciliation report"
  ) {
    Given("I am an agent with a delegated enrolment for an ISA reference")
    val isaReference = generateRandomZReference()
    val agentToken   = agentAuthBearerToken(isaReference)

    When("I generate a reconciliation report using the test support API as the agent")
    val response = generateReport(isaReference, agentToken)

    Then("A 204 status code is returned")
    response.status shouldBe NO_CONTENT
  }

  Scenario(
    s"5. Verify the test support API rejects an agent without a delegated enrolment generating a reconciliation report"
  ) {
    Given("I am an agent with a delegated enrolment for a different ISA reference")
    val isaReference = generateRandomZReference()
    val agentToken   = agentAuthBearerToken(ZReferenceGenerator.generate())

    When("I generate a reconciliation report using the test support API as the agent")
    val response = generateReport(isaReference, agentToken)

    assertAgentClientRelationshipNotFound(response)
  }

  private def generateReport(isaReference: String, token: String): StandaloneWSResponse =
    testSupportService.triggerGenerateReport(isaReference, totalRecords, validHeadersOnlyWithToken(token))

  private def getAllResults(isaReference: String, token: String): (Seq[StandaloneWSResponse], Seq[JsValue]) = {
    val limit                  = 2
    var cursor: Option[String] = None
    var returnResults          = Seq.empty[JsValue]
    var responses              = Seq.empty[StandaloneWSResponse]
    var moreResultsAvailable   = true

    while (moreResultsAvailable) {
      val response = disaReturnsService.getReconciliationReport(
        isaReference,
        validHeadersOnlyWithToken(token),
        cursor = cursor,
        limit = Some(limit)
      )
      val json     = Json.parse(response.body).as[JsObject]

      responses :+= response
      returnResults ++= (json \ "returnResults").as[Seq[JsValue]]

      (json \ "currentPage").toOption        shouldBe None
      (json \ "recordsInPage").toOption      shouldBe None
      (json \ "totalRecords").toOption       shouldBe None
      (json \ "totalNumberOfPages").toOption shouldBe None

      cursor = (json \ "nextCursor").toOption.map(_.as[String])
      cursor.foreach(_.nonEmpty shouldBe true)
      moreResultsAvailable = cursor.nonEmpty
    }

    (responses, returnResults)
  }
}
