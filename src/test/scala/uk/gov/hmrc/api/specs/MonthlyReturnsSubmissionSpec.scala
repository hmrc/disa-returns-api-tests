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
import uk.gov.hmrc.api.utils.MockMonthlyReturnData.validNdjsonTestData

class MonthlyReturnsSubmissionSpec extends BaseSpec, LazyLogging {

  val isaReference: String = generateRandomZReference()
  val authToken: String    = authHelper.getAuthBearerToken(isaReference)

  Scenario(
    s"1. Verify 'submission endpoint' returns a 204 status code for a successful submission"
  ) {

    Given("I have a valid authentication and an ISA reference")

    When("I POST a submission request")
    val submissionResponse: StandaloneWSResponse =
      submissionRequest(authToken, isaManagerReference = isaReference, month = month)

    Then("A 204 status code is returned")
    submissionResponse.status shouldBe 204
  }

  Scenario(
    s"2. Verify submission endpoint accepts NDJSON WITHOUT a trailing newline"
  ) {

    Given("I have a valid authentication and an ISA reference")

    When("I POST a submission request without trailing newline")
    val response: StandaloneWSResponse =
      submissionRequest(
        authToken,
        isaManagerReference = isaReference,
        month = month,
        ndString = validNdjsonTestData().stripSuffix("\n")
      )

    Then("A 204 status code is returned")
    response.status shouldBe 204
  }

  Scenario(
    s"3. Verify submission endpoint accepts up to 10MB NDJSON file in a single request"
  ) {

    Given("I have a valid authentication and an ISA reference")

    When("I POST a submission request with a 10mb request body")
    val response: StandaloneWSResponse =
      submissionRequest(
        authToken,
        isaManagerReference = isaReference,
        month = month,
        ndString = validNdjsonTestData(5818).stripSuffix("\n")
      )

    Then("A 204 status code is returned")
    response.status shouldBe 204
  }

  Scenario(
    s"4. Verify submission endpoint fails to accept more than 10MB NDJSON file in a single request"
  ) {

    Given("I have a valid authentication and an ISA reference")

    When("I POST a submission request with over 10mb request body")
    val response: StandaloneWSResponse =
      submissionRequest(
        authToken,
        isaManagerReference = isaReference,
        month = month,
        ndString = validNdjsonTestData(5845).stripSuffix("\n")
      )

    Then("A 413 status code is returned")
    response.status shouldBe 413
  }
}
