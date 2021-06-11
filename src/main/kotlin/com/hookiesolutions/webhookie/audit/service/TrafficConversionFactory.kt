package com.hookiesolutions.webhookie.audit.service

import com.hookiesolutions.webhookie.audit.domain.SpanRetry
import com.hookiesolutions.webhookie.audit.domain.SpanSendReason
import com.hookiesolutions.webhookie.common.Constants
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import org.springframework.integration.core.MessageSelector
import org.springframework.messaging.Message
import org.springframework.stereotype.Service
import reactor.util.function.Tuple2
import reactor.util.function.Tuples

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 11/6/21 12:23
 */
@Service
class TrafficConversionFactory(
  private val resentMessageSelector: MessageSelector,
  private val unblockedMessageSelector: MessageSelector,
) {
  fun calculateSpanSendDetails(message: Message<SignableSubscriptionMessage>): Tuple2<SpanSendReason, String> {
    val isResend = resentMessageSelector.accept(message)
    val reason = when {
      unblockedMessageSelector.accept(message) -> {
        SpanSendReason.UNBLOCK
      }
      isResend -> {
        SpanSendReason.RESEND
      }
      else -> {
        SpanSendReason.RETRY
      }
    }
    val requestedBy: String = if(isResend) {
      message.headers[Constants.Queue.Headers.WH_HEADER_REQUESTED_BY] as String
    } else {
      SpanRetry.SENT_BY_WEBHOOKIE
    }

    return Tuples.of(reason, requestedBy)
  }
}
