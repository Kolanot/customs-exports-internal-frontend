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

package services.audit

import com.google.inject.Inject
import connectors.exchanges.{ArrivalExchange, MovementExchange}
import forms._
import javax.inject.Named
import models.cache._
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}

import scala.concurrent.{ExecutionContext, Future}

class AuditService @Inject()(connector: AuditConnector, @Named("appName") appName: String)(implicit ec: ExecutionContext) {
  private val logger = Logger(this.getClass)

  def auditShutMucr(providerId: String, mucr: String, result: String)(implicit hc: HeaderCarrier): Future[AuditResult] =
    audit(
      AuditTypes.AuditShutMucr,
      Map(EventData.providerId.toString -> providerId, EventData.mucr.toString -> mucr, EventData.submissionResult.toString -> result)
    )

  def auditDisassociate(providerId: String, ucr: String, result: String)(implicit hc: HeaderCarrier): Future[AuditResult] =
    audit(
      AuditTypes.AuditDisassociate,
      Map(EventData.providerId.toString -> providerId, EventData.ucr.toString -> ucr, EventData.submissionResult.toString -> result)
    )

  def auditAssociate(providerId: String, mucr: String, ducr: String, result: String)(implicit hc: HeaderCarrier): Future[AuditResult] =
    audit(
      AuditTypes.AuditAssociate,
      Map(
        EventData.providerId.toString -> providerId,
        EventData.mucr.toString -> mucr,
        EventData.ducr.toString -> ducr,
        EventData.submissionResult.toString -> result
      )
    )

  def auditMovements(data: MovementExchange, result: String, movementAuditType: AuditTypes.Audit)(implicit hc: HeaderCarrier): Future[AuditResult] =
    audit(
      movementAuditType,
      Map(
        EventData.providerId.toString -> data.providerId,
        EventData.messageCode.toString -> data.choice.toString,
        EventData.ucrType.toString -> data.consignmentReference.reference,
        EventData.ucr.toString -> data.consignmentReference.referenceValue,
        EventData.submissionResult.toString -> result
      ).++(movementReference(data))
    )

  private def movementReference(data: MovementExchange): Map[String, String] = data match {
    case arrival: ArrivalExchange => Map(EventData.movementReference.toString -> arrival.arrivalReference.reference.getOrElse(""))
    case _                        => Map()
  }

  private def audit(auditType: AuditTypes.Audit, auditData: Map[String, String])(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val event = createAuditEvent(auditType, auditData)
    connector.sendEvent(event).map(handleResponse(_, auditType.toString))
  }

  private def createAuditEvent(choice: AuditTypes.Audit, auditData: Map[String, String])(implicit hc: HeaderCarrier) =
    DataEvent(
      auditSource = appName,
      auditType = choice.toString,
      tags = getAuditTags(transactionNameSuffix = s"${choice}-request", path = s"$choice"),
      detail = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails() ++ auditData
    )

  private def getAuditTags(transactionNameSuffix: String, path: String)(implicit hc: HeaderCarrier) =
    AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(transactionName = s"Export-Declaration-${transactionNameSuffix}", path = s"customs-declare-exports/${path}")

  private def handleResponse(result: AuditResult, auditType: String) = result match {
    case Success =>
      logger.debug(s"Exports ${auditType} audit successful")
      Success
    case Failure(err, _) =>
      logger.warn(s"Exports ${auditType} Audit Error, message: $err")
      Failure(err)
    case Disabled =>
      logger.warn(s"Auditing Disabled")
      Disabled
  }

  def auditAllPagesUserInput(answers: Answers)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val auditType = answers.`type` match {
      case JourneyType.ARRIVE               => AuditTypes.AuditArrival.toString
      case JourneyType.RETROSPECTIVE_ARRIVE => AuditTypes.AuditRetrospectiveArrival.toString
      case JourneyType.DEPART               => AuditTypes.AuditDeparture.toString
    }

    val extendedEvent = ExtendedDataEvent(
      auditSource = appName,
      auditType = auditType,
      tags = getAuditTags(s"${auditType}-payload-request", s"${auditType}/full-payload"),
      detail = getAuditDetails(getMovementsData(answers))
    )
    connector.sendExtendedEvent(extendedEvent).map(handleResponse(_, auditType))
  }

  private def getAuditDetails(userInput: JsObject)(implicit hc: HeaderCarrier) = {
    val hcAuditDetails = Json.toJson(AuditExtensions.auditHeaderCarrier(hc).toAuditDetails()).as[JsObject]
    hcAuditDetails.deepMerge(userInput)
  }

  private def getMovementsData(answers: Answers): JsObject = {

    val userInput = answers match {
      case arrivalAnswers: ArrivalAnswers =>
        Map(
          ConsignmentReferences.formId -> Json.toJson(arrivalAnswers.consignmentReferences),
          Location.formId -> Json.toJson(arrivalAnswers.location),
          MovementDetails.formId -> Json.toJson(arrivalAnswers.arrivalDetails),
          ArrivalReference.formId -> Json.toJson(arrivalAnswers.arrivalReference)
        )
      case retroArrivalAnswers: RetrospectiveArrivalAnswers =>
        Map(
          ConsignmentReferences.formId -> Json.toJson(retroArrivalAnswers.consignmentReferences),
          Location.formId -> Json.toJson(retroArrivalAnswers.location)
        )
      case departureAnswers: DepartureAnswers =>
        Map(
          ConsignmentReferences.formId -> Json.toJson(departureAnswers.consignmentReferences),
          Location.formId -> Json.toJson(departureAnswers.location),
          MovementDetails.formId -> Json.toJson(departureAnswers.departureDetails),
          Transport.formId -> Json.toJson(departureAnswers.transport)
        )
    }

    Json.toJson(userInput).as[JsObject]
  }
}

object AuditTypes extends Enumeration {
  type Audit = Value
  val AuditArrival: AuditTypes.Value = Value("Arrival")
  val AuditRetrospectiveArrival: AuditTypes.Value = Value("RetrospectiveArrival")
  val AuditDeparture: AuditTypes.Value = Value("Departure")
  val AuditAssociate: AuditTypes.Value = Value("Associate")
  val AuditDisassociate: AuditTypes.Value = Value("Disassociate")
  val AuditShutMucr: AuditTypes.Value = Value("ShutMucr")
}
object EventData extends Enumeration {
  type Data = Value
  val providerId, mucr, ducr, ucr, ucrType, messageCode, movementReference, submissionResult, Success, Failure = Value
}
