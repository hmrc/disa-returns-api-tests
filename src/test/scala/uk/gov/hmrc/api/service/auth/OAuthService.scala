/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.api.service.auth

import play.api.libs.json.Json
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.constant.AppConfig.baseUrl
import uk.gov.hmrc.api.utils.CustomHttpClient
import uk.gov.hmrc.apitestrunner.util.ApiLogger.log

import scala.concurrent.Await
import scala.concurrent.duration.*

class OAuthService(customHttpClient: CustomHttpClient) {
  val host: String = baseUrl("oauth-api")

  def getBearerToken(clientId: String, clientSecret: String, scope: String): String = {
    val url: String               = s"$host/token"
    log.info(s"Retrieving bearer token from $url")
    val headers: (String, String) = "Content-Type" -> "application/x-www-form-urlencoded"
    val body: String              = s"client_id=$clientId&client_secret=$clientSecret&scope=$scope&grant_type=client_credentials"

    val response: StandaloneWSResponse =
      Await.result(customHttpClient.post(url, body, headers), 10.seconds)

    val token =
      (Json.parse(response.body) \ "access_token").as[String]

    token
  }
}
