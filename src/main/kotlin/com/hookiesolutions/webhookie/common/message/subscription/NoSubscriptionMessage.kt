package com.hookiesolutions.webhookie.common.message.subscription

import com.hookiesolutions.webhookie.common.message.ConsumerMessage

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 7/12/20 00:48
 */
data class NoSubscriptionMessage(
  override val originalMessage: ConsumerMessage
): GenericSubscriptionMessage