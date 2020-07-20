/*
 * Copyright 2020 HM Revenue & Customs
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

package models.submissions

import java.time.Instant
import java.util.UUID

import connectors.exchanges.ActionType
import models.UcrBlock
import models.UcrType.Mucr
import play.api.libs.json._

case class Submission(
  uuid: String = UUID.randomUUID().toString,
  eori: String,
  conversationId: String,
  ucrBlocks: Seq[UcrBlock],
  actionType: ActionType,
  requestTimestamp: Instant = Instant.now()
) {

  def hasMucr: Boolean = ucrBlocks.exists(_.ucrType == Mucr.codeValue)

  def extractMucr: Option[String] = ucrBlocks.find(_.ucrType == Mucr.codeValue).map(_.fullUcr)

  def extractFirstUcr: Option[String] = ucrBlocks.headOption.map(_.fullUcr)
}

object Submission {
  implicit val format: OFormat[Submission] = Json.format[Submission]
}
