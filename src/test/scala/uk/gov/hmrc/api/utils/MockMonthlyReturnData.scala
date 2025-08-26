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

package uk.gov.hmrc.api.utils

import play.api.libs.json.Json
import uk.gov.hmrc.api.models.*

object MockMonthlyReturnData extends NdjsonSupport {

  def getLISANewSubscriptionPayload(
    nino: String,
    accountNumber: String
  ): LifetimeISANewSubscriptionPayload = LifetimeISANewSubscriptionPayload(
    accountNumber,
    nino,
    "First24997",
    "LISA NS model",
    "Last24997",
    "1980-01-22",
    "LIFETIME_CASH",
    false,
    "2025-06-01",
    2500.23,
    10000.12,
    "2025-01-22",
    5000.56,
    3000.56
  )

  def getLISAClosurePayload(
    nino: String,
    accountNumber: String
  ): LiftimeISAClosurePayload = LiftimeISAClosurePayload(
    accountNumber,
    nino,
    "First24998",
    "LISA Closure model",
    "Last24998",
    "1980-01-22",
    "LIFETIME_STOCKS_AND_SHARES",
    false,
    "2025-06-01",
    2500.23,
    10000.12,
    "2025-01-22",
    "2025-03-22",
    "CLOSED",
    5000.56,
    3000.56
  )

  def getLISATransferAndClosurePayload(
    nino: String,
    accountNumber: String
  ): LifetimeISATransferAndClosurePayload = LifetimeISATransferAndClosurePayload(
    accountNumber,
    nino,
    "First24999",
    "LISA Transfer & Closure model",
    "Last24999",
    "1980-01-22",
    "LIFETIME_STOCKS_AND_SHARES",
    true,
    "2025-06-01",
    2500.23,
    10000.12,
    "1234567",
    1200.34,
    "2025-01-22",
    "2025-03-22",
    "TRANSFERRED_IN_FULL",
    5000.56,
    3000.56
  )

  def getLISATransferPayload(
    nino: String,
    accountNumber: String
  ): LifetimeISATransferPayload = LifetimeISATransferPayload(
    accountNumber,
    nino,
    "First25000",
    "LISA Transfer model",
    "Last25000",
    "1980-01-22",
    "LIFETIME_CASH",
    true,
    "2025-06-01",
    2500.23,
    10000.12,
    "1234567",
    1200.34,
    "2025-01-22",
    5000.56,
    3000.56
  )

  def getSISANewSubscriptionPayload(
    nino: String,
    accountNumber: String
  ): StandardISANewSubscriptionPayload = StandardISANewSubscriptionPayload(
    accountNumber,
    nino,
    "First25001",
    "SISA new subscription model",
    "Last25001",
    "1980-01-22",
    "STOCKS_AND_SHARES",
    false,
    "2025-06-01",
    2500.23,
    10000.12,
    false
  )

  def getSISATransferPayload(
    nino: String,
    accountNumber: String
  ): StandardISATransferPayload = StandardISATransferPayload(
    accountNumber,
    nino,
    "First25002",
    "SISA Transfer model",
    "Last25002",
    "1980-01-22",
    "CASH",
    true,
    "2025-06-01",
    2500.23,
    10000.12,
    "1234567",
    1200.34,
    true
  )

  def validNdjsonTestData(): String = {
    val lisaNewSubscriptionPayload    =
      getLISANewSubscriptionPayload(RandomDataGenerator.nino(), RandomDataGenerator.generateSTDCode())
    val lisaClosurePayload            = getLISAClosurePayload(RandomDataGenerator.nino(), RandomDataGenerator.generateSTDCode())
    val lisaTransferAndClosurePayload =
      getLISATransferAndClosurePayload(RandomDataGenerator.nino(), RandomDataGenerator.generateSTDCode())
    val lisaTransferPayload           = getLISATransferPayload(RandomDataGenerator.nino(), RandomDataGenerator.generateSTDCode())
    val sisaNewSubscriptionPayload    =
      getSISANewSubscriptionPayload(RandomDataGenerator.nino(), RandomDataGenerator.generateSTDCode())
    val sisaTransferPayload           = getSISATransferPayload(RandomDataGenerator.nino(), RandomDataGenerator.generateSTDCode())

    toNdjson(
      Seq(
        Json.toJson(lisaNewSubscriptionPayload),
        Json.toJson(lisaClosurePayload),
        Json.toJson(lisaTransferAndClosurePayload),
        Json.toJson(lisaTransferPayload),
        Json.toJson(sisaNewSubscriptionPayload),
        Json.toJson(sisaTransferPayload)
      )
    )
  }
}
