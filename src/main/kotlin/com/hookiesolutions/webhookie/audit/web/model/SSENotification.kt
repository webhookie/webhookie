/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie.audit.web.model

import com.hookiesolutions.webhookie.audit.domain.Span
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder

data class SSENotification(
  val id: String,
  val event: String,
  val data: Any,
) {
  class SpanNotification {
    companion object {
      fun created(span: Span): Message<SSENotification> {
        return message(span, "spanCreated", span)
      }

      fun increasedNumberOfTries(span: Span): Message<SSENotification> {
        return message(span, "spanNumberOfTriesIncreased", span)
      }

      fun blocked(span: Span): Message<SSENotification> {
        return message(span, "spanBlocked", span)
      }

      fun failedWithServerError(span: Span): Message<SSENotification> {
        return message(span, "spanFailedWithServerError", span)
      }

      fun failedWithClientError(span: Span): Message<SSENotification> {
        return message(span, "spanFailedWithClientError", span)
      }

      fun failedWithOtherError(span: Span): Message<SSENotification> {
        return message(span, "spanFailedWithOtherError", span)
      }

      fun failedWithSubscriptionError(span: Span): Message<SSENotification> {
        return message(span, "failedWithSubscriptionError", span)
      }

      fun failedWithStatusUpdate(span: Span): Message<SSENotification> {
        return message(span, "spanFailedStatusUpdate", span)
      }

      fun success(span: Span): Message<SSENotification> {
        return message(span, "spanWasOK", span)
      }

      fun markedRetrying(span: Span): Message<SSENotification> {
        return message(span, "spanMarkedRetrying", span)
      }

      fun isRetrying(span: Span): Message<SSENotification> {
        return message(span, "spanIsRetrying", span)
      }

      private fun message(span: Span, type: String, data: Any): Message<SSENotification> {
        return MessageBuilder
          .withPayload(SSENotification(span.spanId, type, data))
          .build()
      }
    }
  }
}
