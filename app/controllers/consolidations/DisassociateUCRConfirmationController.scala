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

import controllers.actions.AuthenticatedAction
import controllers.storage.FlashExtractor
import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import models.cache.JourneyType.DISSOCIATE_UCR
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.confirmation_page

import scala.concurrent.ExecutionContext

@Singleton
class DisassociateUCRConfirmationController @Inject()(
  authenticate: AuthenticatedAction,
  mcc: MessagesControllerComponents,
  flashExtractor: FlashExtractor,
  confirmationPage: confirmation_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage: Action[AnyContent] = authenticate { implicit request =>
    val journeyType = flashExtractor.extractMovementType(request).getOrElse(throw ReturnToStartException)
    journeyType match {
      case DISSOCIATE_UCR => Ok(confirmationPage(journeyType))
      case _              => throw ReturnToStartException
    }
  }

}
