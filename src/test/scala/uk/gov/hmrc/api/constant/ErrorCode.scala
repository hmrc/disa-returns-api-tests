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

sealed trait ErrorCode {
  def status: Int
  def code: String
  def message: String
}

case object ObligationMet extends ErrorCode {
  val status  = 403
  val code    = "RETURN_OBLIGATION_ALREADY_MET"
  val message = "Return obligation already met"
}

case object InternalServerError extends ErrorCode {
  val status  = 500
  val code    = "INTERNAL_SERVER_ERROR"
  val message = "There has been an issue processing your request"
}

case object BadRequest extends ErrorCode {
  val status  = 400
  val code    = "VALIDATION_FAILURE"
  val message = "Bad request"
}
