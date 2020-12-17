package com.hookiesolutions.webhookie.common.message.publisher

import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/12/20 23:48
 */
data class PublisherSuccessMessage(
  override val subscriptionMessage: SubscriptionMessage,
  val response: ServerResponse
): GenericPublisherMessage
