/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.consolidations

import controllers.actions.{AuthenticatedAction, JourneyRefiner}
import controllers.storage.FlashKeys
import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import models.cache.{JourneyType, ShutMucrAnswers}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.shutmucr.shut_mucr_summary

import scala.concurrent.ExecutionContext

@Singleton
class ShutMucrSummaryController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  submissionService: SubmissionService,
  summaryPage: shut_mucr_summary
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.SHUT_MUCR)) { implicit request =>
    val mucr = request.answersAs[ShutMucrAnswers].shutMucr.map(_.mucr).getOrElse(throw ReturnToStartException)
    Ok(summaryPage(mucr))
  }

  def submit(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.SHUT_MUCR)).async { implicit request =>
    val answers = request.answersAs[ShutMucrAnswers]

    submissionService.submit(request.providerId, answers).map { _ =>
      Redirect(controllers.consolidations.routes.ShutMucrConfirmationController.displayPage())
        .flashing(FlashKeys.MOVEMENT_TYPE -> request.answers.`type`.toString)
    }
  }
}
