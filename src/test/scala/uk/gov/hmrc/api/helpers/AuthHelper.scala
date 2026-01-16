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

package uk.gov.hmrc.api.helpers

import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.service.AuthService

class AuthHelper {

  val authAPI: AuthService = new AuthService

  def getAuthBearerToken(isaReference: String): String = {
    val newToken                                          = authAPI.getAuthorizationCode()
    val newOauthToken                                     = authAPI.getOAuthToken(newToken)
    val authServiceRequestResponse1: StandaloneWSResponse = authAPI.callGGSignIn(isaReference)
    val cookies                                           = authServiceRequestResponse1.cookies.map(c => s"${c.name}=${c.value}").mkString("; ")
    val authServiceRequestResponse2: StandaloneWSResponse = authAPI.getBearerToken(cookies)
    val authTokenRegex                                    = """(?s)data-session-id="authToken".*?<code[^>]*>(.*?)</code>""".r
    val authTokenOpt                                      = authTokenRegex.findFirstMatchIn(authServiceRequestResponse2.body).map(_.group(1))
    authTokenOpt.getOrElse("No authToken found")
  }
}
