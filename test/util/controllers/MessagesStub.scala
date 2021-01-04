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

package controllers

import base.Injector
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.Request

trait MessagesStub {

  private val messagesApi: MessagesApi = MessagesStub.realMessagesApi

  protected implicit def messages(implicit request: Request[_]): Messages =
    new AllMessageKeysAreMandatoryMessages(messagesApi.preferred(request))

  protected def messages(key: String, args: Any*)(implicit request: Request[_]): String = messages(request)(key, args: _*)

  /*
    Fails the test if a view is configured with a message key that doesnt exist in the messages file
   */
  private class AllMessageKeysAreMandatoryMessages(msg: Messages) extends Messages {
    override def messages: Messages = msg.messages

    override def lang: Lang = msg.lang

    override def apply(key: String, args: Any*): String =
      if (msg.isDefinedAt(key))
        msg.apply(key, args: _*)
      else throw new AssertionError(s"Message Key is not configured for {$key}")

    override def apply(keys: Seq[String], args: Any*): String =
      if (keys.exists(key => !msg.isDefinedAt(key)))
        msg.apply(keys, args)
      else throw new AssertionError(s"Message Key is not configured for {$keys}")

    override def translate(key: String, args: Seq[Any]): Option[String] = msg.translate(key, args)

    override def isDefinedAt(key: String): Boolean = msg.isDefinedAt(key)
  }
}

object MessagesStub extends Injector {
  val realMessagesApi = instanceOf[MessagesApi]
}
