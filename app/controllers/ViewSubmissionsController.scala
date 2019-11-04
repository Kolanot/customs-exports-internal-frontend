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

package controllers

import java.time.Instant

import connectors.CustomsDeclareExportsMovementsConnector
import controllers.actions.AuthenticatedAction
import javax.inject.{Inject, Singleton}
import models.notifications.NotificationFrontendModel
import models.submissions.SubmissionFrontendModel
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.view_submissions

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ViewSubmissionsController @Inject()(
  authenticate: AuthenticatedAction,
  connector: CustomsDeclareExportsMovementsConnector,
  mcc: MessagesControllerComponents,
  viewSubmissionsPage: view_submissions
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage: Action[AnyContent] = authenticate.async { implicit request =>
    val providerId = request.providerId

    for {
      submissions <- connector.fetchAllSubmissions(providerId)
      notifications <- Future.sequence(submissions.map(submission => connector.fetchNotifications(submission.conversationId, providerId)))
      sortedNotifications = notifications.map(_.sorted.reverse)
      submissionsWithNotifications = submissions zip sortedNotifications

    } yield Ok(viewSubmissionsPage(sortWithOldestLast(submissionsWithNotifications)))
  }

  private def sortWithOldestLast(submissionsWithNotifications: Seq[(SubmissionFrontendModel, Seq[NotificationFrontendModel])]) =
    submissionsWithNotifications.sortBy(_._1.requestTimestamp)(Ordering[Instant].reverse)

}