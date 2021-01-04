package com.hookiesolutions.webhookie.common.message.publisher

import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/12/20 23:51
 */
data class PublisherOtherErrorMessage(
  override val subscriptionMessage: SignableSubscriptionMessage,
  override val reason: String
): PublisherErrorMessage
