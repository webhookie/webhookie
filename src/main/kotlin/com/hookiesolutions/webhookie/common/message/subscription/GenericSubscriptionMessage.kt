package com.hookiesolutions.webhookie.common.message.subscription

import com.hookiesolutions.webhookie.common.message.ConsumerMessage

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 7/12/20 01:23
 */
interface GenericSubscriptionMessage {
  val originalMessage: ConsumerMessage
  val spanId: String
}