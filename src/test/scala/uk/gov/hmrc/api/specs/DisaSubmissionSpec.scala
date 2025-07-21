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

  Scenario(s"1. Verify DISA Returns monthly header when no obligation and reporting window opened") {
    Given("I created a valid monthly return header file")

    When("I set the reporting windows status as open")
    val reportingWindowResponse: StandaloneWSResponse = disaSubmissionHelper.setReportingWindow(true)

    Then("I got the status code 204 accepting the reporting window is opened")
    reportingWindowResponse.status shouldBe 204

    When("I created a valid client id using third party application")
    val thirdPartyApplicationResponse: StandaloneWSResponse =
      ppnsHelper.createClientApplication(thirdpartyApplicationHadersMap)

    val clientID        = FileReader.readString(thirdPartyApplicationResponse, "details", "clientId")
    val modifiedHeaders = headersMap + ("X-Client-ID" -> clientID)

    Then("I got the status code 201 saying that client id created")
    thirdPartyApplicationResponse.status shouldBe 201

    When("I create a notification box for the created client id")
    val notificationBoxResponse: StandaloneWSResponse =
      ppnsHelper.createNotificationBox(clientID, notificationBoxHadersMap)

    Then("I got the status code 201 saying that box created for the mentioned client id")
    notificationBoxResponse.status shouldBe 201

    When("I update all the subscription fields")
    ppnsHelper.updateSubscriptionFields()

    When("I update all the subscription field values")
    val subscriptionFieldValuesResponse: StandaloneWSResponse = ppnsHelper.updateSubscriptionFieldValues(clientID)

    Then("I got the status code 201 saying that all the subscription field values were updated")
    subscriptionFieldValuesResponse.status shouldBe 201

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(1000, "APR", 2025, "Z451234", modifiedHeaders)

    Then("I got the status code 200 accepting the file upload is successful")
    response.status shouldBe 200

    val returnId = FileReader.readString(response, "returnId")
    val action   = FileReader.readString(response, "action")
    val boxId    = FileReader.readString(response, "boxId")

    logger.info(s"Generated returnId: $returnId")
    logger.info(s"Generated action: $action")
    logger.info(s"Generated boxId: $boxId")
  }

  Scenario(s"2. Verify DISA Returns monthly header with obligation and reporting window opened") {
    Given("I created a valid monthly return header file")

    When("I set the reporting windows status as open")
    disaSubmissionHelper.setReportingWindow(true)

    When("I created a valid client id using third party application")
    val thirdPartyApplicationResponse: StandaloneWSResponse =
      ppnsHelper.createClientApplication(thirdpartyApplicationHadersMap)

    val clientID        = FileReader.readString(thirdPartyApplicationResponse, "details", "clientId")
    val modifiedHeaders = headersMap + ("X-Client-ID" -> clientID)

    Then("I got the status code 201 saying that client id created")
    thirdPartyApplicationResponse.status shouldBe 201

    When("I create a notification box for the created client id")
    val notificationBoxResponse: StandaloneWSResponse =
      ppnsHelper.createNotificationBox(clientID, notificationBoxHadersMap)

    Then("I got the status code 201 saying that box created for the mentioned client id")
    notificationBoxResponse.status shouldBe 201

    When("I update all the subscription fields")
    ppnsHelper.updateSubscriptionFields()

    When("I update all the subscription field values")
    val subscriptionFieldValuesResponse: StandaloneWSResponse = ppnsHelper.updateSubscriptionFieldValues(clientID)

    Then("I got the status code 201 saying that all the subscription field values were updated")
    subscriptionFieldValuesResponse.status shouldBe 201

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(1000, "APR", 2025, "Z1111", modifiedHeaders)

    Then("I got the status code 403 stating an obligation failed")
    response.status shouldBe 403

    val code    = FileReader.readString(response, "code")
    val message = FileReader.readString(response, "message")

    assert(ObligationClosed.code == code, "Incorrect code")
    assert(ObligationClosed.message == message, "Incorrect message")
  }

  Scenario(s"3. Verify DISA Returns monthly header with no obligation and reporting window closed") {
    Given("I created a valid monthly return header file")

    When("I set the reporting windows status as open")
    disaSubmissionHelper.setReportingWindow(false)

    When("I created a valid client id using third party application")
    val thirdPartyApplicationResponse: StandaloneWSResponse =
      ppnsHelper.createClientApplication(thirdpartyApplicationHadersMap)

    val clientID        = FileReader.readString(thirdPartyApplicationResponse, "details", "clientId")
    val modifiedHeaders = headersMap + ("X-Client-ID" -> clientID)

    Then("I got the status code 201 saying that client id created")
    thirdPartyApplicationResponse.status shouldBe 201

    When("I create a notification box for the created client id")
    val notificationBoxResponse: StandaloneWSResponse =
      ppnsHelper.createNotificationBox(clientID, notificationBoxHadersMap)

    Then("I got the status code 201 saying that box created for the mentioned client id")
    notificationBoxResponse.status shouldBe 201

    When("I update all the subscription fields")
    ppnsHelper.updateSubscriptionFields()

    When("I update all the subscription field values")
    val subscriptionFieldValuesResponse: StandaloneWSResponse = ppnsHelper.updateSubscriptionFieldValues(clientID)

    Then("I got the status code 201 saying that all the subscription field values were updated")
    subscriptionFieldValuesResponse.status shouldBe 201

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(1000, "APR", 2025, "Z341231", modifiedHeaders)

    Then("I got the status code 403 stating reporting window closed")
    response.status shouldBe 403

    val code    = FileReader.readString(response, "code")
    val message = FileReader.readString(response, "message")

    assert(ReportingWindowClosed.code == code, "Incorrect code")
    assert(ReportingWindowClosed.message == message, "Incorrect message")
  }

  Scenario(s"4. Verify DISA Returns monthly header with obligation and reporting window closed") {
    Given("I created a valid monthly return header file")

    When("I set the reporting windows status as open")
    disaSubmissionHelper.setReportingWindow(false)

    When("I created a valid client id using third party application")
    val thirdPartyApplicationResponse: StandaloneWSResponse =
      ppnsHelper.createClientApplication(thirdpartyApplicationHadersMap)

    val clientID        = FileReader.readString(thirdPartyApplicationResponse, "details", "clientId")
    val modifiedHeaders = headersMap + ("X-Client-ID" -> clientID)

    Then("I got the status code 201 saying that client id created")
    thirdPartyApplicationResponse.status shouldBe 201

    When("I create a notification box for the created client id")
    val notificationBoxResponse: StandaloneWSResponse =
      ppnsHelper.createNotificationBox(clientID, notificationBoxHadersMap)

    Then("I got the status code 201 saying that box created for the mentioned client id")
    notificationBoxResponse.status shouldBe 201

    When("I update all the subscription fields")
    ppnsHelper.updateSubscriptionFields()

    When("I update all the subscription field values")
    val subscriptionFieldValuesResponse: StandaloneWSResponse = ppnsHelper.updateSubscriptionFieldValues(clientID)

    Then("I got the status code 201 saying that all the subscription field values were updated")
    subscriptionFieldValuesResponse.status shouldBe 201

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(1000, "APR", 2025, "Z1111", modifiedHeaders)

    Then("I got the status code 403 stating that obligation and reporting window failed")
    response.status shouldBe 403

    val code         = FileReader.readString(response, "code")
    val message      = FileReader.readString(response, "message")
    val innerErrors3 = FileReader.readString(response, "errors")

    assert(Forbidden.code == code, "Incorrect code")
    assert(Forbidden.message == message, "Incorrect message")

    val innerErrors: Seq[JsValue] = Json.parse(innerErrors3).as[Seq[JsValue]]

    assert(innerErrors.exists(err => (err \ "code").as[String] == ReportingWindowClosed.code))
    assert(innerErrors.exists(err => (err \ "code").as[String] == ReportingWindowClosed.code))
    assert(innerErrors.exists(err => (err \ "message").as[String] == ReportingWindowClosed.message))
    assert(innerErrors.exists(err => (err \ "code").as[String] == ObligationClosed.code))
    assert(innerErrors.exists(err => (err \ "message").as[String] == ObligationClosed.message))
  }

  Scenario(s"5. Verify DISA Returns monthly header with internal server error") {
    Given("I created a valid monthly return header file")

    When("I set the reporting windows status as open")
    disaSubmissionHelper.setReportingWindow(true)

    When("I created a valid client id using third party application")
    val thirdPartyApplicationResponse: StandaloneWSResponse =
      ppnsHelper.createClientApplication(thirdpartyApplicationHadersMap)

    val clientID        = FileReader.readString(thirdPartyApplicationResponse, "details", "clientId")
    val modifiedHeaders = headersMap + ("X-Client-ID" -> clientID)

    Then("I got the status code 201 saying that client id created")
    thirdPartyApplicationResponse.status shouldBe 201

    When("I create a notification box for the created client id")
    val notificationBoxResponse: StandaloneWSResponse =
      ppnsHelper.createNotificationBox(clientID, notificationBoxHadersMap)

    Then("I got the status code 201 saying that box created for the mentioned client id")
    notificationBoxResponse.status shouldBe 201

    When("I update all the subscription fields")
    ppnsHelper.updateSubscriptionFields()

    When("I update all the subscription field values")
    val subscriptionFieldValuesResponse: StandaloneWSResponse = ppnsHelper.updateSubscriptionFieldValues(clientID)

    Then("I got the status code 201 saying that all the subscription field values were updated")
    subscriptionFieldValuesResponse.status shouldBe 201

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(1000, "APR", 2025, "Z1234", modifiedHeaders)

    Then("I got the status code 500 stating an internal server error")
    response.status shouldBe 500

    val code    = FileReader.readString(response, "code")
    val message = FileReader.readString(response, "message")

    assert(InternalServerError.code == code, "Incorrect code")
    assert(InternalServerError.message == message, "Incorrect message")
  }

  Scenario(s"6. Verify DISA Returns monthly header for bad request") {
    Given("I created a valid monthly return header file")

    When("I set the reporting windows status as open")
    disaSubmissionHelper.setReportingWindow(true)

    When("I created a valid client id using third party application")
    val thirdPartyApplicationResponse: StandaloneWSResponse =
      ppnsHelper.createClientApplication(thirdpartyApplicationHadersMap)

    val clientID        = FileReader.readString(thirdPartyApplicationResponse, "details", "clientId")
    val modifiedHeaders = headersMap + ("X-Client-ID" -> clientID)

    Then("I got the status code 201 saying that client id created")
    thirdPartyApplicationResponse.status shouldBe 201

    When("I create a notification box for the created client id")
    val notificationBoxResponse: StandaloneWSResponse =
      ppnsHelper.createNotificationBox(clientID, notificationBoxHadersMap)

    Then("I got the status code 201 saying that box created for the mentioned client id")
    notificationBoxResponse.status shouldBe 201

    When("I update all the subscription fields")
    ppnsHelper.updateSubscriptionFields()

    When("I update all the subscription field values")
    val subscriptionFieldValuesResponse: StandaloneWSResponse = ppnsHelper.updateSubscriptionFieldValues(clientID)

    Then("I got the status code 201 saying that all the subscription field values were updated")
    subscriptionFieldValuesResponse.status shouldBe 201

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(-1, "APR", 2025, "Z4321", modifiedHeaders)

    Then("I got the status code 400 stating a bad request")
    response.status shouldBe 400

    val code    = FileReader.readString(response, "code")
    val message = FileReader.readString(response, "message")

    assert(BadRequest.code == code, "Incorrect code")
    assert(BadRequest.message == message, "Incorrect message")
  }

  Scenario(s"7. Verify DISA Returns monthly header for bad request") {
    Given("I created a valid monthly return header file")

    When("I set the reporting windows status as open")
    disaSubmissionHelper.setReportingWindow(true)

    When("I created a valid client id using third party application")
    val thirdPartyApplicationResponse: StandaloneWSResponse =
      ppnsHelper.createClientApplication(thirdpartyApplicationHadersMap)

    val clientID        = FileReader.readString(thirdPartyApplicationResponse, "details", "clientId")
    val modifiedHeaders = headersMap + ("X-Client-ID" -> clientID)

    Then("I got the status code 201 saying that client id created")
    thirdPartyApplicationResponse.status shouldBe 201

    When("I create a notification box for the created client id")
    val notificationBoxResponse: StandaloneWSResponse =
      ppnsHelper.createNotificationBox(clientID, notificationBoxHadersMap)

    Then("I got the status code 201 saying that box created for the mentioned client id")
    notificationBoxResponse.status shouldBe 201

    When("I update all the subscription fields")
    ppnsHelper.updateSubscriptionFields()

    When("I update all the subscription field values")
    val subscriptionFieldValuesResponse: StandaloneWSResponse = ppnsHelper.updateSubscriptionFieldValues(clientID)

    Then("I got the status code 201 saying that all the subscription field values were updated")
    subscriptionFieldValuesResponse.status shouldBe 201

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(1000, "APR", 202545, "Z4321", modifiedHeaders)

    Then("I got the status code 400 stating a bad request")
    response.status shouldBe 400

    val code    = FileReader.readString(response, "code")
    val message = FileReader.readString(response, "message")

    assert(BadRequest.code == code, "Incorrect code")
    assert(BadRequest.message == message, "Incorrect message")
  }

  Scenario(s"8. Verify DISA Returns monthly header with invalid bearer token") {
    Given("I created a valid monthly return header file")

    When("I set the reporting windows status as open")
    disaSubmissionHelper.setReportingWindow(true)

    When("I created a valid client id using third party application")
    val thirdPartyApplicationResponse: StandaloneWSResponse =
      ppnsHelper.createClientApplication(thirdpartyApplicationHadersMap)

    val clientID        = FileReader.readString(thirdPartyApplicationResponse, "details", "clientId")
    val modifiedHeaders = headersMap + ("X-Client-ID" -> clientID)

    Then("I got the status code 201 saying that client id created")
    thirdPartyApplicationResponse.status shouldBe 201

    When("I create a notification box for the created client id")
    val notificationBoxResponse: StandaloneWSResponse =
      ppnsHelper.createNotificationBox(clientID, notificationBoxHadersMap)

    Then("I got the status code 201 saying that box created for the mentioned client id")
    notificationBoxResponse.status shouldBe 201

    When("I update all the subscription fields")
    ppnsHelper.updateSubscriptionFields()

    When("I update all the subscription field values")
    val subscriptionFieldValuesResponse: StandaloneWSResponse = ppnsHelper.updateSubscriptionFieldValues(clientID)

    Then("I got the status code 201 saying that all the subscription field values were updated")
    subscriptionFieldValuesResponse.status shouldBe 201

    When("I use the DISA return initialise API to send the header")
    val response: StandaloneWSResponse = disaSubmissionHelper.postHeader(1000, "APR", 202545, "Z4321", modifiedHeaders)

    Then("I got the status code 400 stating a bad request")
    response.status shouldBe 400

    val code    = FileReader.readString(response, "code")
    val message = FileReader.readString(response, "message")

    assert(BadRequest.code == code, "Incorrect code")
    assert(BadRequest.message == message, "Incorrect message")
  }
}
