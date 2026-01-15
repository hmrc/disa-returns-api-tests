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

import com.typesafe.config.{Config, ConfigFactory}
import uk.gov.hmrc.api.conf.TestEnvironment

object AppConfig {
  private val env    = TestEnvironment
  private val config = ConfigFactory.load()

//  val disaReturnsHost: String            = env.url("disa-returns")
  val disaReturnsHost: String            = baseUrl("disa-returns")
//  val disaReturnsTestSupportHost: String = env.url("disa-returns-test-support-api")
  val disaReturnsTestSupportHost: String = baseUrl("disa-returns-test-support-api")
//  val disa_returns_stub_host: String     = env.url("disa-returns-stubs")
  val disa_returns_stub_host: String     = baseUrl("disa-returns-stubs")
  val disaReturnsRoute: String           = "/monthly/"
  val disaReturnsCallbackPath: String    = "/callback/monthly/"
//  val authBaseUrl: String                = env.url("auth")
  val authBaseUrl: String                = baseUrl("auth")

  def baseUrl(service: String): String =
    config.getString(s"${env.environment}.services.$service.url")

//  def baseUrl(service: String): String =
//    environment match {
//      case "local" =>
//        val host = config.getString("local.services.host")
//        val port = config.getInt(s"local.services.$service.port")
//        s"$host:$port"
//
//      case env =>
//        config.getString(s"$env.services.$service.baseUrl")
//    }
}
