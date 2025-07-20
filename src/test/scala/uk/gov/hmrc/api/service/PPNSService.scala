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
import uk.gov.hmrc.api.utils.FileReader.loadJsonFromFile
import uk.gov.hmrc.apitestrunner.http.HttpClient

import scala.concurrent.Await
import scala.concurrent.duration.*

class PPNSService extends HttpClient {
  val third_party_application_host: String = TestEnvironment.url("third-party-application")
  val ppns_host: String                    = TestEnvironment.url("push-pull-notification")
  val api_subscription_fields_host: String = TestEnvironment.url("api-subscription-fields")
  val thirdPartyApplicationPath: String    = "/application"
  val ppnsPath: String                     = "/box"
  val subscriptionPath                     = "/definition/context/disa-returns/version/1.0"
  val subscriptionFieldPath                = "/field/application/clientId/context/disa-returns/version/1.0"

  def createClientApplication(headers: Map[String, String]): StandaloneWSResponse = {
    val payload: String = loadJsonFromFile("ClientApplication")
    Await.result(
      mkRequest(third_party_application_host + thirdPartyApplicationPath)
        .withHttpHeaders(headers.toSeq: _*)
        .post(payload),
      10.seconds
    )
  }

  def createNotificationBox(clientId: String, headers: Map[String, String]): StandaloneWSResponse = {
    val payload       = loadJsonFromFile("NotificationBox")
    val payloadString = payload.replace("clientIdNumber", clientId)
    Await.result(
      mkRequest(ppns_host + ppnsPath)
        .withHttpHeaders(headers.toSeq: _*)
        .put(payloadString),
      10.seconds
    )
  }

  def createSubscriptionField(): StandaloneWSResponse = {
    val payload = loadJsonFromFile("SubscriptionFields")
    Await.result(
      mkRequest(api_subscription_fields_host + subscriptionPath)
        .withHttpHeaders("Content-Type" -> "application/json")
        .put(payload),
      10.seconds
    )
  }

  def createSubscriptionFieldValues(clientId: String): StandaloneWSResponse = {
    val payload = loadJsonFromFile("SubscriptionFieldValues")
    val path    = subscriptionFieldPath.replace("clientId", clientId)
    Await.result(
      mkRequest(api_subscription_fields_host + path)
        .withHttpHeaders("Content-Type" -> "application/json")
        .put(payload),
      10.seconds
    )
  }
}
