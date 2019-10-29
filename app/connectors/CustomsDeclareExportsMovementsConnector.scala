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

package connectors

import config.AppConfig
import connectors.exchanges.Consolidation
import javax.inject.{Inject, Singleton}
import models.requests.MovementRequest
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CustomsDeclareExportsMovementsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)
  private val JsonHeaders = Seq(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON, HeaderNames.ACCEPT -> ContentTypes.JSON)

  def submit(request: MovementRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    httpClient
      .POST[MovementRequest, HttpResponse](appConfig.customsDeclareExportsMovements + "/movements", request, JsonHeaders)
      .andThen {
        case Success(response) =>
          logExchange("Submit Movement", response.body)
        case Failure(exception) =>
          logFailedExchange("Submit Movement", exception)
      }
      .map(_ => (): Unit)

  def submit[T <: Consolidation](request: T)(implicit hc: HeaderCarrier): Future[Unit] =
    httpClient
      .POST[T, HttpResponse](appConfig.customsDeclareExportsMovements + "/consolidation", request, JsonHeaders)
      .andThen {
        case Success(response) =>
          logExchange("Submit Consolidation", response.body)
        case Failure(exception) =>
          logFailedExchange("Submit Consolidation", exception)
      }
      .map(_ => (): Unit)

  private def logExchange[T](`type`: String, payload: T)(implicit fmt: Format[T]): Unit =
    logger.debug(`type` + "\n" + Json.toJson(payload))

  private def logFailedExchange(`type`: String, exception: Throwable): Unit =
    logger.warn(`type` + " failed", exception)
}