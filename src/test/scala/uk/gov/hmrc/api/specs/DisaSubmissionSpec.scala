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
import uk.gov.hmrc.api.constant.{BadRequest, InternalServerError, ObligationMet}

class DisaSubmissionSpec extends BaseSpec, LazyLogging {

  Scenario(s"Verify DISA Returns monthly header") {
    Given("I created a valid monthly return header file")

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(1000, "APR", 2025, "Z451234", headersMap)

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

  Scenario(s"Verify DISA Returns monthly header with obligation") {
    Given("I created a valid monthly return header file")

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(1000, "APR", 2025, "Z1111", headersMap)

    Then("I got the status code 403 stating an obligation failed")
    response.status shouldBe 403

    val json    = Json.parse(response.body)
    val code    = (json \ "code").as[String]
    val message = (json \ "message").as[String]
    assert(ObligationMet.code == code, "Incorrect code")
    assert(ObligationMet.message == message, "Incorrect message")
  }

  Scenario(s"Verify DISA Returns monthly header with internal server error") {
    Given("I created a valid monthly return header file")

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(1000, "APR", 2025, "Z1234", headersMap)

    Then("I got the status code 500 stating an internal server error")
    response.status shouldBe 500

    val json    = Json.parse(response.body)
    val code    = (json \ "code").as[String]
    val message = (json \ "message").as[String]
    assert(InternalServerError.code == code, "Incorrect code")
    assert(InternalServerError.message == message, "Incorrect message")
  }

  Scenario(s"Verify DISA Returns monthly header for bad request") {
    Given("I created a valid monthly return header file")

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(1000, "APR", 2025, "Z4321", headersMap)

    Then("I got the status code 500 stating a bad request")
    response.status shouldBe 500

    val json    = Json.parse(response.body)
    val code    = (json \ "code").as[String]
    val message = (json \ "message").as[String]
    assert(InternalServerError.code == code, "Incorrect code")
    assert(InternalServerError.message == message, "Incorrect message")
  }

  Scenario(s"2. Verify DISA Returns monthly header for bad request") {
    Given("I created a valid monthly return header file")

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(1000, "APR", 202545, "Z4321", headersMap)

    Then("I got the status code 400 stating a bad request")
    response.status shouldBe 400

    val json    = Json.parse(response.body)
    val code    = (json \ "code").as[String]
    val message = (json \ "message").as[String]
    assert(BadRequest.code == code, "Incorrect code")
    assert(BadRequest.message == message, "Incorrect message")
  }

  Scenario(s"Verify DISA Returns monthly submission") {
    Given("I created a valid monthly return file")

    When("I use the DISA return submission API to send monthly returns")
    val response: StandaloneWSResponse = disaSubmissionHelper.postReturns()

    Then("I got the status code 200 accepting the file upload is successful")
    response.status shouldBe 200

  }
}
