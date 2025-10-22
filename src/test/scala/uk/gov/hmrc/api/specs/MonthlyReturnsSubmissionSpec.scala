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

package uk.gov.hmrc.api.specs

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.utils.BaseSpec

class MonthlyReturnsSubmissionSpec extends BaseSpec, LazyLogging {

  Scenario(
    s"1. Verify 'submission endpoint' returns a 204 status code for a successful submission"
  ) {
    val isaReference = generateRandomZReference()
    openReportingWindow()

    When("I POST a submission request")
    val response: StandaloneWSResponse =
      submissionRequest(isaManagerReference = isaReference, month = month)

    Then("A 204 status code is returned")
    response.status shouldBe 204
  }
}
