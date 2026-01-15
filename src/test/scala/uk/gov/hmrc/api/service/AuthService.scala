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

import play.api.libs.ws.DefaultBodyWritables.writeableOf_urlEncodedSimpleForm
import play.api.libs.ws.{StandaloneWSRequest, StandaloneWSResponse}
import uk.gov.hmrc.api.constant.AppConfig._
import uk.gov.hmrc.apitestrunner.http.HttpClient
import play.api.libs.json._
import scala.concurrent.Await
import scala.concurrent.duration.*

class AuthService extends HttpClient {

  val authSignInUrl  = s"$authBaseUrl/auth-login-stub/gg-sign-in"
  val authSessionUrl = s"$authBaseUrl/auth-login-stub/session"
  val oAuthTokenUrl  = s"$authBaseUrl/oauth/token"

  def callGGSignIn(isaReference: String): StandaloneWSResponse = {
    val formData: Map[String, String] = Map(
      "CredID"                              -> "aaa",
      "affinityGroup"                       -> "Organisation",
      "confidenceLevel"                     -> "50",
      "credentialStrength"                  -> "strong",
      "authorityId"                         -> "",
      "redirectionUrl"                      -> authSessionUrl,
      "enrolment[0].name"                   -> "HMRC-DISA-ORG",
      "enrolment[0].taxIdentifier[0].name"  -> "ZREF",
      "enrolment[0].taxIdentifier[0].value" -> isaReference,
      "enrolment[0].state"                  -> "Activated"
    )
    Await.result(
      mkRequest(authSignInUrl)
        .withHttpHeaders(
          "Content-Type" -> "application/x-www-form-urlencoded",
          "Accept"       -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
          "User-Agent"   -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
        )
        .withFollowRedirects(false)
        .post(formData),
      10.seconds
    )
  }

  def getBearerToken(cookies: String): StandaloneWSResponse =
    Await.result(
      wsClient
        .url(authSessionUrl)
        .withHttpHeaders(
          "Accept"     -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
          "User-Agent" -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
          "Cookie"     -> cookies
        )
        .get(),
      10.seconds
    )

  val Code                      = "code"
  val Client_Id                 = "client_id"
  val Client_Secret             = "client_secret"
  val Grant_Type                = "grant_type"
  val Redirect_Url              = "redirect_uri"
  val authorization_code        = "authorization_code"
  val client_credentials        = "client_credentials"
  val acceptHMRCApplicationJson = "application/vnd.hmrc.1.0+json"
  val Scope                     = "scope"

  val accessTokenBody: Map[String, String] = Map(
    Client_Id     -> s"$clientId",
    Client_Secret -> s"$clientSecret",
    Grant_Type    -> "client_credentials",
    Scope         -> "write:isa-returns read:isa-returns",
    Redirect_Url  ->  "urn:ietf:wg:oauth:2.0:oob"
  )

  def getAccessToken(): String =
    env.environment match {
      case "local" =>
        "CSP"

      case _ =>
        val response = Await.result(
          wsClient
            .url(oAuthTokenUrl)
            .withHttpHeaders(
              "Content-Type" -> "application/x-www-form-urlencoded",
              "Accept"       -> "application/json"
            )
            .post(accessTokenBody),
          10.seconds
        )

        if (response.status != 200) {
          throw new RuntimeException(
            s"Failed to get token. Status=${response.status}, body=${response.body}"
          )
        }

        val json        = Json.parse(response.body)
        val accessToken = (json \ "access_token")
          .asOpt[String]
          .getOrElse(throw new RuntimeException("access_token not found"))
        accessToken
    }

  def getOAuthToken(accessToken: String): String =
    val oAuthTokenBody: Map[String, String] = Map(
      Client_Id     -> s"$clientId",
      Client_Secret -> s"$clientSecret",
      Grant_Type    -> "authorization_code",
      Scope         -> "write:isa-returns read:isa-returns",
      Code          -> "accessToken"
    )

    env.environment match {
      case "local" =>
        "CSP"

      case _ =>
        val response = Await.result(
          wsClient
            .url(oAuthTokenUrl)
            .withHttpHeaders(
              "Content-Type" -> "application/x-www-form-urlencoded",
              "Accept"       -> "application/json"
            )
            .post(oAuthTokenBody),
          10.seconds
        )

        if (response.status != 200) {
          throw new RuntimeException(
            s"Failed to get token. Status=${response.status}, body=${response.body}"
          )
        }

        val json        = Json.parse(response.body)
        println(Console.MAGENTA + json + Console.RESET)
        val accessToken = (json \ "access_token")
          .asOpt[String]
          .getOrElse(throw new RuntimeException("access_token not found"))
        accessToken
    }
}
