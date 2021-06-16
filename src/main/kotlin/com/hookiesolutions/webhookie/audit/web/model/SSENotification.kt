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
