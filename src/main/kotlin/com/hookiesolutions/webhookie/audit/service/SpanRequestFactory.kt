package com.hookiesolutions.webhookie.audit.service

import com.hookiesolutions.webhookie.audit.domain.SubscriptionRequest
import com.hookiesolutions.webhookie.common.message.subscription.RetryableSubscriptionMessage
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component

@Component
class SpanRequestFactory {
  fun createFrom(message: RetryableSubscriptionMessage): SubscriptionRequest {
    val headers = HttpHeaders()
    message.addMessageHeaders(headers)

    return SubscriptionRequest(
      headers.toSingleValueMap(),
      message.originalMessage.contentType,
      message.originalMessage.payload.decodeToString()
    )
  }
}
