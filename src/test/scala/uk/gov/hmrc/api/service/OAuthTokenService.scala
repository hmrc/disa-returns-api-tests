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

package uk.gov.hmrc.api.service

import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.api.constant.AppConfig.{clientId, clientSecret}
import uk.gov.hmrc.apitestrunner.http.HttpClient
import play.api.libs.ws.DefaultBodyWritables.writeableOf_String

import scala.concurrent.Await
import scala.concurrent.duration.*

object OAuthTokenService extends HttpClient {

  def exchangeCodeForToken(
    authorizationCode: String,
    redirectUri: String
  ): OAuthTokenResponse = {

    val body =
      s"""
         |grant_type=authorization_code
         |&code=$authorizationCode
         |&redirect_uri=$redirectUri
         |&client_id=$clientId
         |&client_secret=$clientSecret
       """.stripMargin.replace("\n", "")

    val response = Await.result(
      mkRequest("https://api.development.tax.service.gov.uk/oauth/token")
        .withHttpHeaders(
          "Content-Type" -> "application/x-www-form-urlencoded"
        )
        .post(body),
      10.seconds
    )

    response.status shouldBe 200

    Json.parse(response.body).as[OAuthTokenResponse]
  }
}

case class OAuthTokenResponse(
  access_token: String,
  refresh_token: String,
  expires_in: Int,
  scope: String,
  token_type: String
)
object OAuthTokenResponse {
  implicit val tokenReads: Reads[OAuthTokenResponse] = Json.reads[OAuthTokenResponse]
}
