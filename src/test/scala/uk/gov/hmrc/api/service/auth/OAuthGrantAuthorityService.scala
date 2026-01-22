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

  private lazy val authLoginBase = TestEnvironment.url("auth")
  private lazy val oAuthApiBase  = TestEnvironment.url("oauth-api")

  def generateOAuthAccessToken(zReference: String): String = {

    /** POST /auth-login-stub/gg-sign-in to create auth session with a DISA enrolment */
    val authLoginRedirectResponse = postAuthLoginStub(zReference = zReference)

    /** GET /oauth/authorize using authLoginRedirectResponse redirect location */
    val getOAuthAuthorizeResponse = getOAuthAuthorize(oAuthRedirectLocation = authLoginRedirectResponse._2)

    /** Extract authId and cookies from getOAuthAuthorizeResponse * */
    val authId: String       = extractAuthId(locationHeader = getOAuthAuthorizeResponse._2)
    val sessionCookies       = authLoginRedirectResponse._1.cookies
    val cookieHeader: String =
      sessionCookies.map(c => s"${c.name}=${c.value}").mkString("; ")

    /** GET /oauth/grantscope?auth_id=??? */
    val getGrantAuthorityResponse = getGrantAuthority(authId = authId, cookies = cookieHeader)

    /** Extract csrfToken and cookies from getGrantAuthorityResponse */
    val csrfToken                    = extractCsrfToken(getGrantAuthorityResponse)
    val grantAuthorityCookies        = getGrantAuthorityResponse.cookies
    val grantAuthMdtpCookies: String =
      grantAuthorityCookies.map(c => s"${c.name}=${c.value}").mkString("; ")

    /** POST /oauth/grantscope */
    val postGrantAuthorityResponse =
      postGrantAuthority(authId = authId, csrfToken = csrfToken, grantAuthMdtpCookies = grantAuthMdtpCookies)

    /** Extract authorisationCode from postGrantAuthorityResponse */
    val oAuthCode: String = extractAuthorisationCode(postGrantAuthorityResponse)

    /** Exchange authorisationCode for AccessToken */
    exchangeAccessToken(oAuthCode)
  }

  def postAuthLoginStub(zReference: String): (StandaloneWSResponse, String) = {
    val oAuthRedirectUri =
      s"/oauth/authorize?client_id=$clientId&redirect_uri=$oAuthRedirectUrl&scope=$scopes&response_type=code"

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

    val locationHeader = (loginResponse.status, loginResponse.header("Location")) match {
      case (303, Some(location)) => location
      case (status, None)        =>
        throw new RuntimeException(s"Expected Location header but it was missing. Status: $status")
      case (status, Some(_))     =>
        throw new RuntimeException(s"Unexpected status $status from /auth-login-stub/gg-sign-in")
    }

    (loginResponse, locationHeader)
  }

  def getOAuthAuthorize(oAuthRedirectLocation: String): (StandaloneWSResponse, String) = {
    val response = Await.result(
      httpClient.get(
        s"$authLoginBase$oAuthRedirectLocation",
        headers = "Accept" -> "text/html"
      ),
      10.seconds
    )

    val locationHeader = (response.status, response.header("Location")) match {
      case (303, Some(location)) =>
        location
      case (status, None)        =>
        throw new RuntimeException(s"Expected Location header but it was missing. Status: $status")
      case (status, Some(_))     =>
        throw new RuntimeException(s"Unexpected status $status from $oAuthRedirectLocation")
    }

    (response, locationHeader)
  }

  def extractAuthId(locationHeader: String): String =
    "auth_id=([^&]+)".r
      .findFirstMatchIn(locationHeader)
      .map(_.group(1))
      .getOrElse {
        throw new RuntimeException(s"auth_id not found in Location header: $locationHeader")
      }

  def getGrantAuthority(authId: String, cookies: String): StandaloneWSResponse = {
    val response = Await.result(
      httpClient.get(
        s"$authLoginBase/oauth/grantscope?auth_id=$authId",
        headers = "Accept" -> "text/html",
        "Cookie" -> cookies
      ),
      10.seconds
    )

    response.status match {
      case 200    => response
      case status =>
        throw new RuntimeException(
          s"Expected 200 from /oauth/grantscope?auth_id=$authId, received: $status"
        )
    }
  }

  def postGrantAuthority(authId: String, csrfToken: String, grantAuthMdtpCookies: String) = {
    val response = Await.result(
      httpClient.postForm(
        s"$authLoginBase/oauth/grantscope",
        headers = "Cookie" -> grantAuthMdtpCookies,
        "Content-Type" -> "application/x-www-form-urlencoded",
        formData = Map(
          "auth_id"   -> authId,
          "csrfToken" -> csrfToken
        )
      ),
      10.seconds
    )
    response.status match {
      case 200    => response
      case status =>
        throw new RuntimeException(
          s"Expected 200 from POST /oauth/grantscope, received: ${response.status}"
        )
    }
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

  private def extractAuthorisationCode(response: StandaloneWSResponse): String = {
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
