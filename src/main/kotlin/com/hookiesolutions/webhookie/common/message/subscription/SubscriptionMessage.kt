package com.hookiesolutions.webhookie.common.message.subscription

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/12/20 17:42
 */
data class SubscriptionMessage(
  override val originalMessage: ConsumerMessage,
  override val spanId: String,
  val subscription: SubscriptionDTO,
): GenericSubscriptionMessage
