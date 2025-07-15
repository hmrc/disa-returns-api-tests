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

class DisaSubmissionSpec extends BaseSpec, LazyLogging {

  Scenario(s"Verify DISA Returns monthly header") {
    Given("I created a valid monthly return header file")

    When("I use the DISA return initialise API to send the header")
    val authBearerToken: String        = authHelper.getAuthBearerToken
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(1000, "APR", 2025, authBearerToken)

    Then("I got the status code 200 accepting the file upload is successful")
    response.status shouldBe 200

    val json     = Json.parse(response.body)
    val returnId = (json \ "returnId").as[String]
    val action   = (json \ "action").as[String]
    val boxId    = (json \ "boxId").as[String]
    logger.info(s"Generated returnId: $returnId")
    logger.info(s"Generated action: $action")
    logger.info(s"Generated boxId: $boxId")
  }

  Scenario(s"Verify DISA Returns monthly submission") {
    Given("I created a valid monthly return file")

    When("I use the DISA return submission API to send monthly returns")
    val response: StandaloneWSResponse = disaSubmissionHelper.postReturns()

    Then("I got the status code 200 accepting the file upload is successful")
    response.status shouldBe 200

  }
}
