package com.hookiesolutions.webhookie.common.message.subscription

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import java.time.Duration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/12/20 17:42
 */
data class UnsignedSubscriptionMessage(
  override val originalMessage: ConsumerMessage,
  override val spanId: String,
  override val subscription: SubscriptionDTO,
  override val delay: Duration = Duration.ZERO,
  override val numberOfRetries: Int = 0,
  override val subscriptionIsBlocked: Boolean = subscription.isBlocked,
  override val subscriptionIsWorking: Boolean = !subscription.isBlocked
): SignableSubscriptionMessage {
  override fun retryableCopy(delay: Duration, numberOfRetries: Int): UnsignedSubscriptionMessage {
    return this.copy(
      delay = delay,
      numberOfRetries = numberOfRetries
    )
  }
}

