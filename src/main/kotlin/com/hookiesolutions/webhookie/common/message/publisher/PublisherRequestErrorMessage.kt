package com.hookiesolutions.webhookie.common.message.publisher

import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import org.springframework.http.HttpHeaders

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/12/20 23:50
 */
data class PublisherRequestErrorMessage(
  override val subscriptionMessage: SubscriptionMessage,
  override val reason: String,
  val headers: HttpHeaders,
  override val isRetryable: Boolean = true
): PublisherErrorMessage
