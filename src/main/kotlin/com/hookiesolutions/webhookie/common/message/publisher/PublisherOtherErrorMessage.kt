package com.hookiesolutions.webhookie.common.message.publisher

import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/12/20 23:51
 */
data class PublisherOtherErrorMessage(
  override val subscriptionMessage: SubscriptionMessage,
  override val reason: String,
  override val isRetryable: Boolean = false
): PublisherErrorMessage
