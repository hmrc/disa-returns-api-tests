/*
 * Copyright 2026 HM Revenue & Customs
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

import uk.gov.hmrc.api.constant.TestConstants.*
import uk.gov.hmrc.api.models.UserType
import uk.gov.hmrc.api.utils.RandomDataGenerator.generateAgentReferenceNumber

object AuthLoginStubFormData {

  def apply(
    zReference: String,
    credentialId: String,
    userType: UserType,
    redirectionUrl: String
  ): Map[String, String] = {
    val common = Map(
      "confidenceLevel"    -> "50",
      "credentialStrength" -> "strong",
      "authorityId"        -> credentialId,
      "redirectionUrl"     -> redirectionUrl
    )

    userType match {
      case UserType.Organisation =>
        common ++ Map(
          "affinityGroup"                       -> "Organisation",
          "enrolment[0].name"                   -> EnrolmentKey,
          "enrolment[0].taxIdentifier[0].name"  -> IdentifierKey,
          "enrolment[0].taxIdentifier[0].value" -> zReference,
          "enrolment[0].state"                  -> "Activated"
        )
      case UserType.Agent        =>
        common ++ Map(
          "affinityGroup"                                -> "Agent",
          "enrolment[0].name"                            -> AgentEnrolmentKey,
          "enrolment[0].taxIdentifier[0].name"           -> AgentIdentifierKey,
          "enrolment[0].taxIdentifier[0].value"          -> generateAgentReferenceNumber(),
          "enrolment[0].state"                           -> "Activated",
          "delegatedEnrolment[0].key"                    -> EnrolmentKey,
          "delegatedEnrolment[0].taxIdentifier[0].name"  -> IdentifierKey,
          "delegatedEnrolment[0].taxIdentifier[0].value" -> zReference,
          "delegatedEnrolment[0].delegatedAuthRule"      -> DelegatedAuthRule
        )
    }
  }
}
