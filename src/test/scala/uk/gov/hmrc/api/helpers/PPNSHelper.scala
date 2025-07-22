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
import uk.gov.hmrc.api.service.PPNSService

class PPNSHelper {
  val PPNSService: PPNSService = new PPNSService

  def createClientApplication(headers: Map[String, String]): StandaloneWSResponse =
    PPNSService.createClientApplication(headers)

  def createNotificationBox(clientId: String, headers: Map[String, String]): StandaloneWSResponse =
    PPNSService.createNotificationBox(clientId, headers)

  def updateSubscriptionFields(): StandaloneWSResponse =
    PPNSService.createSubscriptionField()

  def updateSubscriptionFieldValues(clientId: String): StandaloneWSResponse =
    PPNSService.createSubscriptionFieldValues(clientId)

}
