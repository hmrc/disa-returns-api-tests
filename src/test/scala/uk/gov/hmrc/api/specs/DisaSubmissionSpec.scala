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
import uk.gov.hmrc.api.utils.FileReader

class DisaSubmissionSpec extends BaseSpec, LazyLogging {

  Scenario(
    s"1. Verify 'monthly returns submission' api response gives status code 204 when obligation has not met and reporting window is opened"
  ) {
    Given("I set the reporting windows as opened successfully")
    disaReturnsStubHelper.setReportingWindow(true)

    When("I execute 'Initialise returns submission' api")
    val isaReferenceId                 = "Z451234"
    val response: StandaloneWSResponse =
      disaSubmissionHelper.postInitialiseReturnsSubmissionApi(
        1000,
        "APR",
        2025,
        isaReferenceId,
        headersMapWithValidClientIDAndToken
      )

    Then("I got the status code 200")
    response.status shouldBe 200
    val returnId = FileReader.readString(response, "returnId")

    assert(returnId != null, "Return Id is Null")
    assert(FileReader.readString(response, "action") != null, "Return Id is Null")
    assert(FileReader.readString(response, "boxId") != null, "Return Id is Null")

    When("I submit monthly returns first submission")
    val monthlyReturnsSubmissionResponse: StandaloneWSResponse =
      monthlyReturnsSubmissionHelper.postMonthlyReturns(
        isaReferenceId,
        returnId,
        headersMapWithValidClientIDAndTokenWithoutContentType
      )
    Then("I got the status code 204")
    monthlyReturnsSubmissionResponse.status shouldBe 204

    When("I submit monthly return second submission")
    val monthlyReturnsSubmissionResponse2: StandaloneWSResponse =
      monthlyReturnsSubmissionHelper.postMonthlyReturns(
        isaReferenceId,
        returnId,
        headersMapWithValidClientIDAndTokenWithoutContentType
      )
    Then("I got the status code 204")
    monthlyReturnsSubmissionResponse2.status shouldBe 204
  }

  Scenario(
    s"2. Verify 'Initialise returns submission' api response gives status code 403 when obligation has met and reporting window is opened"
  ) {
    Given("I set the reporting windows as opened successfully")
    disaReturnsStubHelper.setReportingWindow(true)

    When("I execute 'Initialise returns submission' api")
    val response: StandaloneWSResponse =
      disaSubmissionHelper.postInitialiseReturnsSubmissionApi(
        1000,
        "APR",
        2025,
        "Z1111",
        headersMapWithValidClientIDAndToken
      )

    Then("I got the status code 403 stating an obligation failed")
    response.status shouldBe 403

    val code    = FileReader.readString(response, "code")
    val message = FileReader.readString(response, "message")

    assert(ObligationClosed.code == code, "Incorrect code")
    assert(ObligationClosed.message == message, "Incorrect message")
  }

  Scenario(
    s"3. Verify 'Initialise returns submission' api response gives status code 403 when no obligation has met and reporting window is closed"
  ) {
    Given("I set the reporting windows as closed successfully")
    disaReturnsStubHelper.setReportingWindow(false)

    When("I execute 'Initialise returns submission' api")
    val response: StandaloneWSResponse =
      disaSubmissionHelper.postInitialiseReturnsSubmissionApi(
        1000,
        "APR",
        2025,
        "Z341231",
        headersMapWithValidClientIDAndToken
      )

    Then("I got the status code 403 stating the reporting window is closed")
    response.status shouldBe 403

    val code    = FileReader.readString(response, "code")
    val message = FileReader.readString(response, "message")

    assert(ReportingWindowClosed.code == code, "Incorrect code")
    assert(ReportingWindowClosed.message == message, "Incorrect message")
  }

  Scenario(
    s"4. Verify 'Initialise returns submission' api response gives status code 403 when obligation has met and reporting window is closed"
  ) {
    Given("I set the reporting windows as closed successfully")
    disaReturnsStubHelper.setReportingWindow(false)

    When("I execute 'Initialise returns submission' api")
    val response: StandaloneWSResponse =
      disaSubmissionHelper.postInitialiseReturnsSubmissionApi(
        1000,
        "APR",
        2025,
        "Z1111",
        headersMapWithValidClientIDAndToken
      )

    Then("I got the status code 403 stating that the obligation and reporting window is failed")
    response.status shouldBe 403

    val code         = FileReader.readString(response, "code")
    val message      = FileReader.readString(response, "message")
    val innerErrors3 = FileReader.readString(response, "errors")

    assert(Forbidden.code == code, "Incorrect code")
    assert(Forbidden.message == message, "Incorrect message")

    val innerErrors: Seq[JsValue] = Json.parse(innerErrors3).as[Seq[JsValue]]

    assert(innerErrors.exists(err => (err \ "code").as[String] == ReportingWindowClosed.code))
    assert(innerErrors.exists(err => (err \ "message").as[String] == ReportingWindowClosed.message))
    assert(innerErrors.exists(err => (err \ "code").as[String] == ObligationClosed.code))
    assert(innerErrors.exists(err => (err \ "message").as[String] == ObligationClosed.message))

  }

  Scenario(
    s"5. Verify 'Initialise returns submission' api response gives status code 500 for an internal server error correctly when etmp returns downstream error"
  ) {
    Given("I set the reporting windows as opened successfully")
    disaReturnsStubHelper.setReportingWindow(true)

    When("I execute 'Initialise returns submission' api")
    val response: StandaloneWSResponse =
      disaSubmissionHelper.postInitialiseReturnsSubmissionApi(
        1000,
        "APR",
        2025,
        "Z1234",
        headersMapWithValidClientIDAndToken
      )

    Then("I got the status code 500 stating an internal server error")
    response.status shouldBe 500

    val code    = FileReader.readString(response, "code")
    val message = FileReader.readString(response, "message")

    assert(InternalServerError.code == code, "Incorrect code")
    assert(InternalServerError.message == message, "Incorrect message")
  }

  Scenario(
    s"6. Verify 'Initialise returns submission' api response gives status code '400 - bad request' for an invalid payload (invalid totalRecords)"
  ) {
    Given("I set the reporting windows as opened successfully")
    disaReturnsStubHelper.setReportingWindow(true)

    When("I execute 'Initialise returns submission' api with an invalid no of totalRecords")
    val response: StandaloneWSResponse =
      disaSubmissionHelper.postInitialiseReturnsSubmissionApi(-1, "APR", 2025, "Z4321", headersMapWithIncorrectClientId)

    Then("I got the status code 400 stating a bad request")
    response.status shouldBe 400

    val code    = FileReader.readString(response, "code")
    val message = FileReader.readString(response, "message")

    assert(BadRequest.code == code, "Incorrect code")
    assert(BadRequest.message == message, "Incorrect message")
  }

  Scenario(
    s"8. Verify 'Initialise returns submission' api response gives status code '400 - invalid bearer token' error when an invalid bearer token used"
  ) {
    Given("I set the reporting windows as opened successfully")
    disaReturnsStubHelper.setReportingWindow(true)

    When("I execute 'Initialise returns submission' api with an invalid token")
    val response: StandaloneWSResponse =
      disaSubmissionHelper.postInitialiseReturnsSubmissionApi(
        1000,
        "APR",
        2025,
        "Z432112",
        headersMapWithIncorrectClientIdAndIncorrectToken
      )

    Then("I got the status code 400 stating a bad request")
    response.status shouldBe 400

    val code    = FileReader.readString(response, "code")
    val message = FileReader.readString(response, "message")

    assert(InvalidBearerToken.code == code, "Incorrect code")
    assert(InvalidBearerToken.message == message, "Incorrect message")
  }
}
