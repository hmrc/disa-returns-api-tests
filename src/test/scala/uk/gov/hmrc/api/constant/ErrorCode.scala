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

case object ObligationClosed extends ErrorCode {
  val status  = 403
  val code    = "OBLIGATION_CLOSED"
  val message = "Obligation closed"
}

case object ReportingWindowClosed extends ErrorCode {
  val status  = 403
  val code    = "REPORTING_WINDOW_CLOSED"
  val message = "Reporting window has been closed"
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

case object InvalidNdJsonPayload extends ErrorCode {
  val status  = 400
  val code    = "BAD_REQUEST"
  val message = "NDJSON payload is empty."
}

case object InvalidBearerToken extends ErrorCode {
  val status  = 401
  val code    = "UNAUTHORISED"
  val message = "Unauthorised"
}

case object Forbidden extends ErrorCode {
  val status  = 403
  val code    = "FORBIDDEN"
  val message = "Multiple issues found regarding your submission"
}

case object MismatchRecordCount extends ErrorCode {
  val status  = 400
  val code    = "MISMATCH_EXPECTED_VS_RECEIVED"
  val message = "Number of records declared in the header does not match the number submitted."
}
