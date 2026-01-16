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

package uk.gov.hmrc.api.constant

import com.typesafe.config.ConfigFactory
import uk.gov.hmrc.api.conf.TestEnvironment

object AppConfig {
  val env: TestEnvironment.type = TestEnvironment
  private val config            = ConfigFactory.load()

  val disaReturnsHost: String            = baseUrl("disa-returns")
  val disaReturnsTestSupportHost: String = baseUrl("disa-returns-test-support-api")
  val disa_returns_stub_host: String     = baseUrl("disa-returns-stubs")
  val authBaseUrl: String                = baseUrl("auth")
  val authApiBaseUrl: String             = baseUrl("auth-api")
  val apiPlatformTestUserUrl: String     = baseUrl("api-platform-test-user")
  val oAuthApi: String                   = baseUrl("oauth-api")

  val disaReturnsRoute: String        = "/monthly/"
  val disaReturnsCallbackPath: String = "/callback/monthly/"

  val clientId: String         = config.getString(s"environments.${env.environment}.clientId")
  val clientSecret: String     = config.getString(s"environments.${env.environment}.clientSecret")
  val oAuthRedirectUrl: String = config.getString(s"environments.${env.environment}.redirectUrl")
  val scopes: String           = config.getString(s"environments.${env.environment}.scopes")

  def baseUrl(service: String): String =
    config.getString(s"environments.${env.environment}.$service")
}
