/*
 * Copyright 2019 HM Revenue & Customs
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

package forms

import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import services.Countries.allCountries
import utils.FieldValidator._

case class Location(code: String)

object Location {

  implicit val format: OFormat[Location] = Json.format[Location]

  val formId = "Location"

  /**
    * Country is in two first characters in Location Code
    */
  private val validateCountry: String => Boolean = (input: String) => {
    val countryCode = input.take(2)
    allCountries.exists(_.countryCode == countryCode)
  }

  /**
    * Location Type is defined as third character in Location Code
    */
  private val validateLocationType: String => Boolean = (input: String) => {
    val correctLocationType: Set[String] = Set("A", "B", "C", "D")
    val predicate = isContainedIn(correctLocationType)
    input.drop(2).headOption.map(_.toString).exists(predicate)
  }

  /**
    * Qualifier Code is defined in fourth characted in Location Code
    */
  private val validateQualifierCode: String => Boolean = (input: String) => {
    val correctQualifierCode: Set[String] = Set("U", "Y")
    val predicate = isContainedIn(correctQualifierCode)
    input.drop(3).headOption.map(_.toString).exists(predicate)
  }

  val mapping = Forms.mapping(
    "code" -> text()
      .verifying("location.code.empty", nonEmpty)
      .verifying(
        "location.code.error",
        isEmpty or (
          validateCountry and validateLocationType and validateQualifierCode and noShorterThan(10) and noLongerThan(17)
        )
      )
  )(Location.apply)(Location.unapply)

  def form(): Form[Location] = Form(mapping)

}