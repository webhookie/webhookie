package com.hookiesolutions.webhookie.audit.web.model.response

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.domain.SpanRetry
import com.hookiesolutions.webhookie.audit.domain.SpanStatusUpdate
import com.hookiesolutions.webhookie.common.model.dto.CallbackDTO

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/3/21 00:11
 */
data class SpanResponse(
  val traceId: String,
  val spanId: String,
  val application: String,
  val entity: String,
  val topic: String,
  val callback: CallbackDTO,
  val responseCode: Int,
  val responseBody: String,
  val status: SpanStatusUpdate,
  val tries: Int,
  val nextRetry: SpanRetry?
) {
  companion object {
    fun from(span: Span): SpanResponse {
      val responseCode = span.latestResult?.statusCode ?: -1
      val responseBody = span.latestResult?.body ?: ""
      return SpanResponse(
        span.traceId,
        span.spanId,
        span.subscription.application.name,
        span.subscription.application.entity,
        span.subscription.topic,
        span.subscription.callback,
        responseCode,
        responseBody,
        span.lastStatus,
        span.statusHistory.size,
        span.nextRetry
      )
    }
  }
}
