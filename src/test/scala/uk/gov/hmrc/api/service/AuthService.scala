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
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.conf.TestEnvironment
import uk.gov.hmrc.apitestrunner.http.HttpClient

import scala.concurrent.Await
import scala.concurrent.duration.*

class AuthService extends HttpClient {

  val host: String = TestEnvironment.url("auth")
  val url          = s"$host/auth-login-stub/gg-sign-in"

  def callGGSignIn(isaReference: String): StandaloneWSResponse = {
    val formData: Map[String, String] = Map(
      "CredID"                              -> "aaa",
      "affinityGroup"                       -> "Organisation",
      "confidenceLevel"                     -> "50",
      "credentialStrength"                  -> "strong",
      "authorityId"                         -> "",
      "redirectionUrl"                      -> "http://localhost:9949/auth-login-stub/session",
      "enrolment[0].name"                   -> "HMRC-DISA-ORG",
      "enrolment[0].taxIdentifier[0].name"  -> "ZREF",
      "enrolment[0].taxIdentifier[0].value" -> isaReference,
      "enrolment[0].state"                  -> "Activated"
    )
    Await.result(
      mkRequest(url)
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
        .url("http://localhost:9949/auth-login-stub/session")
        .withHttpHeaders(
          "Accept"     -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
          "User-Agent" -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
          "Cookie"     -> cookies
        )
        .get(),
      10.seconds
    )
}
