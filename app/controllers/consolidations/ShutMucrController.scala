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

package controllers.consolidations

import controllers.actions.{AuthenticatedAction, JourneyRefiner}
import controllers.consolidations.{routes => consolidationsRoutes}
import forms.ShutMucr
import forms.ShutMucr.form
import javax.inject.{Inject, Singleton}
import models.cache.{Cache, JourneyType, ShutMucrAnswers}
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.shut_mucr

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ShutMucrController @Inject()(
  authenticate: AuthenticatedAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  cacheRepository: CacheRepository,
  shutMucrPage: shut_mucr
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.SHUT_MUCR)) { implicit request =>
    val shutMucr: Option[ShutMucr] = request.answersAs[ShutMucrAnswers].shutMucr
    Ok(shutMucrPage(shutMucr.fold(form())(form().fill)))
  }

  def submit(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.SHUT_MUCR)).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(shutMucrPage(formWithErrors))),
        validForm => {
          val updatedCache = request.answersAs[ShutMucrAnswers].copy(shutMucr = Some(validForm))
          cacheRepository.upsert(request.cache.update(updatedCache)).map { _ =>
            Redirect(consolidationsRoutes.ShutMucrSummaryController.displayPage())
          }
        }
      )
  }
}
