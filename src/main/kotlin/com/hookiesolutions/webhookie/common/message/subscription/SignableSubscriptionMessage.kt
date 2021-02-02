package com.hookiesolutions.webhookie.common.message.subscription

import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import java.time.Duration

interface SignableSubscriptionMessage: GenericSubscriptionMessage {
  val spanId: String
  val subscription: SubscriptionDTO
  val delay: Duration
  val numberOfRetries: Int
  val subscriptionIsBlocked: Boolean
  val subscriptionIsWorking: Boolean

  fun retryableCopy(
    delay: Duration,
    numberOfRetries: Int
  ): SignableSubscriptionMessage

  val isSignable: Boolean
    get() = subscription.callback.isSignable
}