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
import uk.gov.hmrc.api.utils.BaseSpec

class MonthlyReturnsDeclarationSpec extends BaseSpec, LazyLogging {

  Scenario(
    s"1. Verify 'declaration endpoint' returns a 200 status code for a successful request"
  ) {
    openReportingWindow()

    Given("I have a valid authentication and an ISA reference")
    val isaReference      = generateRandomZReference()
    val authToken: String = authHelper.getAuthBearerToken(isaReference)

    When("I POST a declaration request")
    val response = declarationRequest(authToken, isaReference, taxYear = taxYear, month = month)

    Then("A 200 status code is returned")
    response.status shouldBe 200
    val body = Json.parse(response.body)
    (body \ "boxId").asOpt[String] shouldBe empty
  }

  Scenario(
    s"2. Verify 'declaration endpoint' returns a 200 status code and successfully returns a boxId"
  ) {
    openReportingWindow()

    Given("I have a valid authentication and an ISA reference")
    val isaReference      = generateRandomZReference()
    val authToken: String = authHelper.getAuthBearerToken(isaReference)

    Given("A client application and PPNS box is created")
    createClientApplication(authToken)
    createNotificationBoxAndSubscribe(authToken)

    When("I POST a declaration request")
    val response = declarationRequest(authToken, isaReference, taxYear = taxYear, month = month)

    Then("A 200 status code is returned")
    response.status shouldBe 200
    val body = Json.parse(response.body)
    (body \ "boxId").asOpt[String] should not be empty
  }

  Scenario(
    s"3. Verify 'declaration endpoint' returns a 200 status code for a nil return"
  ) {
    openReportingWindow()

    Given("I have a valid authentication and an ISA reference")
    val isaReference      = generateRandomZReference()
    val authToken: String = authHelper.getAuthBearerToken(isaReference)

    When("I POST a declaration request for a nil return")
    val response = declarationRequest(authToken, isaReference, taxYear = taxYear, month = month, nilReturn = true)

    Then("A 200 status code is returned")
    response.status shouldBe 200
  }

}
