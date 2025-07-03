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

import scala.util.Random

object RandomDataGenerator {
  def nino(): String = {
    val firstTwoLetters = "ABCEGHJKLMNPRSTWXYZ"
    val letterPart      = Random.shuffle(firstTwoLetters.toList).take(2).mkString
    val numberPart      = Random.nextInt(999999).toString.reverse.padTo(6, '0').reverse
    val lastLetters     = "ABCD"
    val lastLetterPart  = Random.shuffle(lastLetters.toList).take(1).mkString
    s"$letterPart$numberPart$lastLetterPart"
  }

  def generateSTDCode(): String = {
    val number = Random.nextInt(999999) + 1
    f"STD$number%06d"
  }

  def generateOLDCode(): String = {
    val number = Random.nextInt(999999) + 1
    f"OLD$number%06d"
  }
}
