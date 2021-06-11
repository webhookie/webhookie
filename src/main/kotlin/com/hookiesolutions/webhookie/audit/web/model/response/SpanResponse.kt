package com.hookiesolutions.webhookie.audit.web.model.response

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.domain.SpanRetry
import com.hookiesolutions.webhookie.audit.domain.SpanStatusUpdate
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDetails

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/3/21 00:11
 */
data class SpanResponse(
  val traceId: String,
  val spanId: String,
  val subscription: SubscriptionDetails,
  val responseCode: Int,
  val status: SpanStatusUpdate,
  val tries: Int,
  val nextRetry: SpanRetry?
) {
  companion object {
    fun from(span: Span): SpanResponse {
      val responseCode = span.latestResult?.statusCode ?: -1
      return SpanResponse(
        span.traceId,
        span.spanId,
        span.subscription,
        responseCode,
        span.lastStatus,
        span.totalNumberOfTries,
        span.nextRetry
      )
    }
  }
}
