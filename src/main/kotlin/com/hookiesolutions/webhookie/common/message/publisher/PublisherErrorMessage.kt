package com.hookiesolutions.webhookie.common.message.publisher

import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/12/20 23:51
 */
data class PublisherErrorMessage(
  override val subscriptionMessage: SubscriptionMessage,
  val reason: String
): GenericPublisherMessage
