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

package views.components

import config.AppConfig
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import views.html.components.confirmation_link
import views.{ViewMatchers, ViewSpec}

class ConfirmationLinkViewSpec extends ViewSpec with ViewMatchers with MockitoSugar {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val appConfig = mock[AppConfig]

  private def linkComponent = new confirmation_link(appConfig)

  "Confirmation link component" should {

    "render link to 'choice' page" when {

      "config does not contain feature switch" in {

        val linkView = linkComponent()
          .getElementsByClass("govuk-link")
          .first()
        linkView must haveHref(controllers.routes.ChoiceController.displayPage())
        linkView must containMessage("movement.confirmation.redirect.choice.link")
      }

      "config contains 'ileQuery' feature switch OFF" in {

        when(appConfig.hasIleQueryFeature).thenReturn(false)

        val linkView = linkComponent()
          .getElementsByClass("govuk-link")
          .first()
        linkView must haveHref(controllers.routes.ChoiceController.displayPage())
        linkView must containMessage("movement.confirmation.redirect.choice.link")
      }

    }

    "render link to 'find consignment' page" when {

      "config contains 'ileQuery' feature switch ON" in {

        when(appConfig.hasIleQueryFeature).thenReturn(true)

        val linkView = linkComponent()
          .getElementsByClass("govuk-link")
          .first()
        linkView must haveHref(controllers.ileQuery.routes.IleQueryController.displayQueryForm())
        linkView must containMessage("movement.confirmation.redirect.query.link")
      }

    }

  }
}