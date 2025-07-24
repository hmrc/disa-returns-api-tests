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
import uk.gov.hmrc.api.service.DisaReturnService

class DisaSubmissionHelper {

  val disaSubmissionService: DisaReturnService = new DisaReturnService

  def postInitialiseReturnsSubmissionApi(
    totalRecords: Int,
    submissionPeriod: String,
    taxYear: Int,
    isaManagerReference: String,
    headers: Map[String, String]
  ): StandaloneWSResponse =
    disaSubmissionService.postInitialiseReturnsSubmissionApi(
      totalRecords,
      submissionPeriod,
      taxYear,
      isaManagerReference,
      headers
    )

  def submitBulkMonthlyReturns(isaManagerReference: String, returnId: String): StandaloneWSResponse =
    disaSubmissionService.submitBulkMonthlyReturns(isaManagerReference, returnId)

}
