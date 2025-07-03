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

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.*

import scala.io.Source

object FileUtils extends LazyLogging {

  def updateNdjsonWithNino(fileName: String): String = {
    val source = Source.fromResource("NDJsons/request/" + fileName + ".txt")
    val sb     = new StringBuilder

    try
      for ((line, i) <- source.getLines().zipWithIndex) {
        val trimmed = line.trim
        if (trimmed.nonEmpty) {
          try {
            val jsValue = Json.parse(trimmed)
            jsValue.validate[JsObject] match {
              case JsSuccess(jsObject, _) =>
                val updatedPayload =
                  jsObject + ("nino"                         -> JsString(RandomDataGenerator.nino())) + ("accountNumber" -> JsString(
                    RandomDataGenerator.generateSTDCode()
                  )) + ("accountNumberOfTransferringAccount" -> JsString(RandomDataGenerator.generateOLDCode()))
                sb.append(Json.stringify(updatedPayload)).append("\n")
              case JsError(errors)        =>
                logger.info(s"[WARN] Line ${i + 1}: Invalid JSON object, skipping. Errors: $errors")
            }
          } catch {
            case ex: Exception =>
              logger.info(s"[ERROR] Line ${i + 1}: Failed to parse JSON, skipping. Error: ${ex.getMessage}")
          }
        }
      }
    finally
      source.close()
    sb.toString()
  }
}
