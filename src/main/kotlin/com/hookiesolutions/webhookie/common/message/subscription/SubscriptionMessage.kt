package com.hookiesolutions.webhookie.common.message.subscription

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import java.time.Duration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/12/20 17:42
 */
data class SubscriptionMessage(
  override val originalMessage: ConsumerMessage,
  val spanId: String,
  val subscription: SubscriptionDTO,
  val delay: Duration = Duration.ZERO,
  val numberOfRetries: Int = 0
): GenericSubscriptionMessage
