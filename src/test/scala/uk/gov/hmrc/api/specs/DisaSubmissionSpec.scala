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
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.constant.*

class DisaSubmissionSpec extends BaseSpec, LazyLogging {

  Scenario(s"1. Verify DISA Returns monthly header when no obligation and reporting window opened") {
    Given("I created a valid monthly return header file")

    When("I set the reporting windows status as open")
    val reportingWindowResponse: StandaloneWSResponse = disaSubmissionHelper.setReportingWindow(true)

    Then("I got the status code 204 accepting the reporting window is opened")
    reportingWindowResponse.status shouldBe 204

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

  Scenario(s"2. Verify DISA Returns monthly header with obligation and reporting window opened") {
    Given("I created a valid monthly return header file")

    When("I set the reporting windows status as open")
    disaSubmissionHelper.setReportingWindow(true)

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(1000, "APR", 2025, "Z1111", headersMap)

    Then("I got the status code 403 stating an obligation failed")
    response.status shouldBe 403

    val json    = Json.parse(response.body)
    val code    = (json \ "code").as[String]
    val message = (json \ "message").as[String]
    assert(ObligationClosed.code == code, "Incorrect code")
    assert(ObligationClosed.message == message, "Incorrect message")
  }

  Scenario(s"3. Verify DISA Returns monthly header with no obligation and reporting window closed") {
    Given("I created a valid monthly return header file")

    When("I set the reporting windows status as open")
    disaSubmissionHelper.setReportingWindow(false)

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(1000, "APR", 2025, "Z341231", headersMap)

    Then("I got the status code 403 stating reporting window closed")
    response.status shouldBe 403

    val json    = Json.parse(response.body)
    val code    = (json \ "code").as[String]
    val message = (json \ "message").as[String]
    assert(ReportingWindowClosed.code == code, "Incorrect code")
    assert(ReportingWindowClosed.message == message, "Incorrect message")
  }

  Scenario(s"4. Verify DISA Returns monthly header with obligation and reporting window closed") {
    Given("I created a valid monthly return header file")

    When("I set the reporting windows status as open")
    disaSubmissionHelper.setReportingWindow(false)

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(1000, "APR", 2025, "Z1111", headersMap)

    Then("I got the status code 403 stating that obligation and reporting window failed")
    response.status shouldBe 403

    val json        = Json.parse(response.body)
    val code        = (json \ "code").as[String]
    val message     = (json \ "message").as[String]
    assert(Forbidden.code == code, "Incorrect code")
    assert(Forbidden.message == message, "Incorrect message")
    val innerErrors = (json \ "errors").as[Seq[JsValue]]
    assert(innerErrors.exists(err => (err \ "code").as[String] == ReportingWindowClosed.code))
    assert(innerErrors.exists(err => (err \ "message").as[String] == ReportingWindowClosed.message))
    assert(innerErrors.exists(err => (err \ "code").as[String] == ObligationClosed.code))
    assert(innerErrors.exists(err => (err \ "message").as[String] == ObligationClosed.message))
  }

  Scenario(s"5. Verify DISA Returns monthly header with internal server error") {
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

  Scenario(s"6. Verify DISA Returns monthly header for bad request") {
    Given("I created a valid monthly return header file")

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(-1, "APR", 2025, "Z4321", headersMap)

    Then("I got the status code 400 stating a bad request")
    response.status shouldBe 400

    val json    = Json.parse(response.body)
    val code    = (json \ "code").as[String]
    val message = (json \ "message").as[String]
    assert(BadRequest.code == code, "Incorrect code")
    assert(BadRequest.message == message, "Incorrect message")
  }

  Scenario(s"7. Verify DISA Returns monthly header for bad request") {
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

  Scenario(s"7. Verify DISA Returns monthly header with invalid bearer token") {
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

  Scenario(s"8. Verify DISA Returns monthly submission") {
    Given("I created a valid monthly return file")

    When("I use the DISA return submission API to send monthly returns")
    val response: StandaloneWSResponse = disaSubmissionHelper.postReturns()

    Then("I got the status code 200 accepting the file upload is successful")
    response.status shouldBe 200

  }
}
