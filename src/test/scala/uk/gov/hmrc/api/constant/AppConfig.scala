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
  private val env    = TestEnvironment
  private val config = ConfigFactory.load()

  println(Console.MAGENTA + env.environment + Console.RESET)
  val disaReturnsHost: String            = baseUrlFor("disa-returns")
  val disaReturnsTestSupportHost: String = baseUrlFor("disa-returns-test-support-api")
  val disaReturnsRoute: String           = "/monthly/"
  val disaReturnsCallbackPath: String    = "/callback/monthly/"

  private def urlFor(protocol: String, host: String, port: String) =
    if (port.toInt == 80 || port.toInt == 443) s"$protocol://$host" else s"$protocol://$host:$port"

  def baseUrlFor(serviceName: String): String = {
    val protocol: String =
      if (config.hasPath(s"${env.environment}.services.$serviceName.protocol"))
        config.getString(s"${env.environment}.services.$serviceName.protocol")
      else "http"
    val host             = config.getString(s"${env.environment}.services.$serviceName.host")
    val port             = config.getString(s"${env.environment}.services.$serviceName.port")

    val hostOrDefault = if (host.isEmpty) "localhost" else host
    val portOrDefault = if (port.isEmpty) "80" else port

    urlFor(protocol, host, portOrDefault)
  }

  def serviceIsDefined(protocol: String, host: String, port: String): Boolean =
    !protocol.isEmpty || !host.isEmpty || !port.isEmpty

  case class ConfigNotFoundException(message: String) extends RuntimeException(message)
}
