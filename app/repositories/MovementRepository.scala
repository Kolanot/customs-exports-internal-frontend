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

package repositories

import javax.inject.Inject
import models.cache.Cache
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.objectIdFormats

import scala.concurrent.{ExecutionContext, Future}

class MovementRepository @Inject()(mc: ReactiveMongoComponent)(implicit ec: ExecutionContext)
    extends ReactiveRepository[Cache, BSONObjectID]("movementCache", mc.mongoConnector.db, Cache.format, objectIdFormats) {

  override def indexes: Seq[Index] = Seq(Index(Seq("pid" -> IndexType.Ascending), name = Some("pidIdx")))

  def findByPid(pid: String): Future[Option[Cache]] = find("pid" -> pid).map(_.headOption)

  def removeByPid(pid: String): Future[Unit] = remove("pid" -> pid).filter(_.ok).map(_ => (): Unit)

  def findOrCreate(pid: String, onMissing: Cache): Future[Cache] =
    findByPid(pid).flatMap {
      case Some(movementCache) => Future.successful(movementCache)
      case None                => save(onMissing)
    }

  private def save(movementCache: Cache): Future[Cache] = insert(movementCache).map { res =>
    if (!res.ok) logger.error(s"Errors when persisting movement cache: ${res.writeErrors.mkString("--")}")
    movementCache
  }

  def upsert(movementCache: Cache): Future[Cache] =
    findAndUpdate(Json.obj("pid" -> movementCache.pid), Json.toJson(movementCache).as[JsObject])
      .map(_.value.map(_.as[Cache]))
      .flatMap {
        case Some(cache) => Future.successful(cache)
        case None        => save(movementCache)
      }
}