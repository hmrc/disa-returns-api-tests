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

class DisaReturnsDeclarationSpec extends BaseSpec, LazyLogging {

  Scenario(
    s"1. Verify 'declaration endpoint' returns a 200 status code for a successful request"
  ) {
    val isaReference = generateRandomZReference()
    openReportingWindow()
    When("I POST a declaration request")
    val response     = declarationRequest(isaReference, taxYear = taxYear, month = month)

    Then("A 200 status code is returned")
    response.status shouldBe 200
    val body = Json.parse(response.body)
    (body \ "boxId").asOpt[String] shouldBe empty
  }

  Scenario(
    s"2. Verify 'declaration endpoint' returns a 200 status code and successfully returns a boxId"
  ) {
    val isaReference = generateRandomZReference()
    openReportingWindow()
    Given("A client application and PPNS box is created")
    createClientApplication()
    createNotificationBoxAndSubscribe()

    When("I POST a declaration request")
    val response = declarationRequest(isaReference, taxYear = taxYear, month = month)

    Then("A 200 status code is returned")
    response.status shouldBe 200
    val body = Json.parse(response.body)
    (body \ "boxId").asOpt[String] should not be empty
  }

}
