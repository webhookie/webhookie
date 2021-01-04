package com.hookiesolutions.webhookie.common.message.publisher

import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import org.springframework.http.HttpHeaders

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/12/20 23:50
 */
data class PublisherRequestErrorMessage(
  override val subscriptionMessage: SignableSubscriptionMessage,
  override val reason: String,
  val headers: HttpHeaders
): PublisherErrorMessage
