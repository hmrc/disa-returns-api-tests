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

import play.api.libs.ws.StandaloneWSResponse

class DisaSubmissionSpec extends BaseSpec {

  Scenario(s"Verify DISA Returns monthly submission") {
    Given("I created a valid monthly return file")
    val filename = "Submission1"

    When("I use the DISA return submission API to send monthly returns")
    val response: StandaloneWSResponse = disaSubmissionHelper.post(filename)

    Then("I got the status code 200 accepting the file upload is successful")
    response.status shouldBe 200

  }
}
