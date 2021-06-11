package com.hookiesolutions.webhookie.common.message.subscription

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.domain.Trace
import com.hookiesolutions.webhookie.common.message.ConsumerMessage

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/6/21 21:15
 */
data class ResendSpanMessage(
  val spanId: String,
  val subscriptionId: String,
  val consumerMessage: ConsumerMessage,
  val totalNumberOfTries: Int,
  val requestedBy: String
) {
  companion object {
    fun create(
      span: Span,
      trace: Trace,
      requestedBy: String
    ): ResendSpanMessage {
      val totalNumberOfTries = span.totalNumberOfTries + 1
      return ResendSpanMessage(
        span.spanId,
        span.subscription.subscriptionId,
        trace.consumerMessage,
        totalNumberOfTries,
        requestedBy
      )
    }
  }
}
