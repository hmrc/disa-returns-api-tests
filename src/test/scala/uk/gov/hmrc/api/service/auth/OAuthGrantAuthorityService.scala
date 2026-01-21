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

import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.conf.TestEnvironment
import uk.gov.hmrc.api.constant.AppConfig.*
import uk.gov.hmrc.api.utils.CustomHttpClient
import uk.gov.hmrc.apitestrunner.util.ApiLogger.log

import scala.collection.immutable
import scala.concurrent.Await
import scala.concurrent.duration.*

class OAuthGrantAuthorityService(httpClient: CustomHttpClient) {

  private val authLoginBase = TestEnvironment.url("auth")
  private val oAuthApiBase  = TestEnvironment.url("oauth-api")

  /** Perform OAuth grant-authority flow and return session cookies (mdtp, etc.)
    */
  def grantAuthorityAndReturnSessionCookies(zReference: String) = {
    val authToken = loginAndGetAccessToken(zReference)
    exchangeAccessToken(authToken)
  }

  private def loginAndGetAccessToken(zReference: String) = {
    val oAuthRedirectUri =
      s"/oauth/authorize?client_id=$clientId&redirect_uri=$oAuthRedirectUrl&scope=$scopes&response_type=code"

    /** POST /auth-login-stub/gg-sign-in to simulate a user session */

    val formData: Map[String, String] = Map(
      "CredID"                              -> "aaa",
      "affinityGroup"                       -> "Organisation",
      "confidenceLevel"                     -> "50",
      "credentialStrength"                  -> "strong",
      "authorityId"                         -> "",
      "redirectionUrl"                      -> oAuthRedirectUri,
      "enrolment[0].name"                   -> "HMRC-DISA-ORG",
      "enrolment[0].taxIdentifier[0].name"  -> "ZREF",
      "enrolment[0].taxIdentifier[0].value" -> zReference,
      "enrolment[0].state"                  -> "Activated"
    )

    val loginResponse = Await.result(
      httpClient.postForm(
        s"$authLoginBase/auth-login-stub/gg-sign-in",
        formData,
        headers = "Accept" -> "text/html"
      ),
      10.seconds
    )

    val authLoginResponseLocation: String =
      loginResponse
        .header("Location")
        .getOrElse(throw new RuntimeException("Location header missing from login response"))

    /** Calls GET /oauth/authorize?client_id=???&redirect_uri=???&scope=???&response_type=code & redirected to
      * /oauth/start?auth_id=??? *
      */
    val authLoginResponse = Await.result(
      httpClient.get(
        s"$authLoginBase$authLoginResponseLocation",
        headers = "Accept" -> "text/html"
      ),
      10.seconds
    )

    if (authLoginResponse.status != 303)
      throw new RuntimeException(
        s"Expected 303 from /oauth/authorize, got ${authLoginResponse.status}"
      )

    val authorizeResponseLocation: String =
      authLoginResponse
        .header("Location")
        .getOrElse(throw new RuntimeException("Location header missing from /oauth/authorize response"))

    /** On /oauth/start?auth_id=??? extract authId and cookies * */
    val AUTH_ID_PATTERN = "auth_id=([^&]+)".r

    val authId: String =
      AUTH_ID_PATTERN
        .findFirstMatchIn(authorizeResponseLocation)
        .map(_.group(1))
        .getOrElse {
          throw new RuntimeException(s"auth_id not found in Location header: $authorizeResponseLocation")
        }

    val sessionCookies = loginResponse.cookies

    val cookieHeader: String =
      sessionCookies.map(c => s"${c.name}=${c.value}").mkString("; ")

    /** Call the GET /oauth/grantscope?auth_id=??? */
    val getGrantAuthorityResponse = Await.result(
      httpClient.get(
        s"$authLoginBase/oauth/grantscope?auth_id=$authId",
        headers = "Accept" -> "text/html",
        "Cookie" -> cookieHeader
      ),
      10.seconds
    )

    if (getGrantAuthorityResponse.status != 200)
      throw new RuntimeException(
        s"Expected 200 from /oauth/grantscope?auth_id=$authId, got ${authLoginResponse.status}"
      )

    /** Extract csrfToken and cookies from getGrantAuthorityResponse */
    val csrfToken = extractCsrfToken(getGrantAuthorityResponse)

    val grantAuthorityCookies = getGrantAuthorityResponse.cookies

    val mdtpCookieGrantAuthCookieHeader: String =
      grantAuthorityCookies.map(c => s"${c.name}=${c.value}").mkString("; ")

    /** POST /oauth/grantscope with extracted csrfToken */
    val postGrantAuthorityResponse = Await.result(
      httpClient.postForm(
        s"$authLoginBase/oauth/grantscope",
        headers = "Cookie" -> mdtpCookieGrantAuthCookieHeader,
        "Content-Type" -> "application/x-www-form-urlencoded",
        formData = Map(
          "auth_id"   -> authId,
          "csrfToken" -> csrfToken
        )
      ),
      10.seconds
    )

    if (postGrantAuthorityResponse.status != 200)
      throw new RuntimeException(
        s"Expected 200 from POST /oauth/grantscope, got ${postGrantAuthorityResponse.status}"
      )

    /** extract authId from postGrantAuthorityResponse */
    extractOauthCode(postGrantAuthorityResponse)

  }

  def exchangeAccessToken(authCode: String): String = {
    val formData: Map[String, String] = Map(
      "grant_type"    -> "authorization_code",
      "client_id"     -> clientId,
      "client_secret" -> clientSecret,
      "redirect_uri"  -> oAuthRedirectUrl,
      "code"          -> authCode
    )

    val response: StandaloneWSResponse = Await.result(
      httpClient.postForm(
        s"$oAuthApiBase/token",
        formData,
        headers = "Accept" -> "application/vnd.hmrc.1.0+json"
      ),
      10.seconds
    )

    if (response.status != 200) {
      throw new RuntimeException(
        s"Failed to exchange auth code for access token, status=${response.status}, body=${response.body}"
      )
    }

    val accessToken = (Json.parse(response.body) \ "access_token").asOpt[String].getOrElse {
      throw new RuntimeException(s"access_token not found in response: ${response.body}")
    }
    s"Bearer $accessToken"
  }

  private def extractOauthCode(response: StandaloneWSResponse): String = {
    val doc = Jsoup.parse(response.body)
    doc.select("#authorisation-code").text() match {
      case code if code.nonEmpty => code
      case _                     => throw new RuntimeException("OAuth code not found in HTML page")
    }
  }

  private def extractCsrfToken(grantAuthorityResponse: StandaloneWSResponse): String = {
    val csrfToken =
      Jsoup
        .parse(grantAuthorityResponse.body)
        .select("input[name=csrfToken]")
        .attr("value")

    if (csrfToken.isEmpty)
      throw new RuntimeException("CSRF token not found in grant authority page")

    log.info(s"Extracted CSRF token: ${csrfToken.take(8)}... (truncated)")
    csrfToken
  }
}
