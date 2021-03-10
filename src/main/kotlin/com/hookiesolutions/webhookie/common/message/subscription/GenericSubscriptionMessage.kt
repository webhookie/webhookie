package com.hookiesolutions.webhookie.common.message.subscription

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.WebhookieMessage
import org.springframework.http.HttpHeaders

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 7/12/20 01:23
 */
interface GenericSubscriptionMessage: WebhookieMessage {
  val originalMessage: ConsumerMessage

  fun addMessageHeaders(headers: HttpHeaders) {
    originalMessage.addMessageHeaders(headers)
  }

  override val traceId: String
    get() = originalMessage.traceId
}
