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

package uk.gov.hmrc.api.service

import play.api.libs.ws.DefaultBodyWritables.writeableOf_String
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.conf.TestEnvironment
import uk.gov.hmrc.apitestrunner.http.HttpClient

import scala.concurrent.Await
import scala.concurrent.duration.*

class PPNSService extends HttpClient {
  val third_party_application_host: String = TestEnvironment.url("third-party-application")
  val ppns_host: String                    = TestEnvironment.url("push-pull-notifications-api")
  val api_subscription_fields_host: String = TestEnvironment.url("api-subscription-fields")
  val thirdPartyApplicationPath: String    = "/application"
  val ppnsPath: String                     = "/box"

  val subscriptionPath                 = "/definition/context/obligation%2Fdeclaration%2Fisa%2Freturn/version/1.0"
  val subscriptionFieldPath            = "/field/application/clientId/context/obligation%2Fdeclaration%2Fisa%2Freturn/version/1.0"
  val clientApplicationPayload: String = """{
                                       |  "name": "TEST APP",
                                       |  "access": {
                                       |    "accessType": "STANDARD",
                                       |    "redirectUris": [],
                                       |    "overrides": []
                                       |  },
                                       |  "environment": "SANDBOX",
                                       |  "collaborators": [
                                       |    {
                                       |      "emailAddress": "test@test.com",
                                       |      "role": "ADMINISTRATOR",
                                       |      "userId": "dfdf62b2-5f07-29d9-9302-45cd2e5eb49b"
                                       |    }
                                       |  ]
                                       |}""".stripMargin

  val notificationBoxPayload: String = """{
                                     |  "boxName": "obligations/declaration/isa/return##1.0##callbackUrl",
                                     |  "clientId": "clientIdNumber"
                                     |}""".stripMargin

  val subscriptionFieldsPayload: String = """{
                                    |  "fieldDefinitions": [
                                    |    {
                                    |      "name": "callbackUrl",
                                    |      "shortDescription": "Notification URL",
                                    |      "description": "What is your notification web address for us to send push notifications to?",
                                    |      "type": "PPNSField",
                                    |      "hint": "You must only give us a web address that you own. Your application will use this address to listen to notifications from HMRC.",
                                    |      "validation": {
                                    |        "errorMessage": "notificationUrl must be a valid https URL",
                                    |        "rules": [
                                    |          {
                                    |            "UrlValidationRule": {}
                                    |          }
                                    |        ]
                                    |      }
                                    |    }
                                    |  ]
                                    |}""".stripMargin

  val subscriptionFieldValuesPayload: String = """{
                                         |  "fields": {
                                         |    "callbackUrl": "http://localhost:10202/push-pull-notification-receiver-stub/notifications"
                                         |  }
                                         |}""".stripMargin

  def createClientApplication(headers: Map[String, String]): StandaloneWSResponse =
    Await.result(
      mkRequest(third_party_application_host + thirdPartyApplicationPath)
        .withHttpHeaders(headers.toSeq: _*)
        .post(clientApplicationPayload),
      10.seconds
    )

  def createNotificationBox(clientId: String, headers: Map[String, String]): StandaloneWSResponse = {
    val payload       = notificationBoxPayload
    val payloadString = payload.replace("clientIdNumber", clientId)
    Await.result(
      mkRequest(ppns_host + ppnsPath)
        .withHttpHeaders(headers.toSeq: _*)
        .put(payloadString),
      10.seconds
    )
  }

  def createSubscriptionField(): StandaloneWSResponse =
    Await.result(
      mkRequest(api_subscription_fields_host + subscriptionPath)
        .withHttpHeaders("Content-Type" -> "application/json")
        .put(subscriptionFieldsPayload),
      10.seconds
    )

  def createSubscriptionFieldValues(clientId: String): StandaloneWSResponse =
    val path = subscriptionFieldPath.replace("clientId", clientId)
    Await.result(
      mkRequest(api_subscription_fields_host + path)
        .withHttpHeaders("Content-Type" -> "application/json")
        .put(subscriptionFieldValuesPayload),
      10.seconds
    )
}
