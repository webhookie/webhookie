package com.hookiesolutions.webhookie.common.message.publisher

import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/12/20 23:49
 */
data class PublisherResponseErrorMessage(
  override val subscriptionMessage: SubscriptionMessage,
  val response: ServerResponse,
  override val reason: String
): PublisherErrorMessage