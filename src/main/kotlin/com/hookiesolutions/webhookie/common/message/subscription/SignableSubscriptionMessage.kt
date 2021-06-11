package com.hookiesolutions.webhookie.common.message.subscription

import com.hookiesolutions.webhookie.common.message.WebhookieSpanMessage
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import java.time.Duration
interface SignableSubscriptionMessage: GenericSubscriptionMessage, WebhookieSpanMessage {
  override val spanId: String
  val subscription: SubscriptionDTO
  val delay: Duration
  val numberOfRetries: Int
  val totalNumberOfTries: Int
  val subscriptionIsBlocked: Boolean
  val subscriptionIsWorking: Boolean

  fun retryableCopy(
    delay: Duration,
    numberOfRetries: Int
  ): SignableSubscriptionMessage

  val isSignable: Boolean
    get() = subscription.callback.isSignable

  fun isNew(): Boolean {
    return totalNumberOfTries == 1
  }

  fun isResend(): Boolean {
    return totalNumberOfTries > 1
  }

  fun isFirstRetry(): Boolean {
    return totalNumberOfTries == 2
  }

  fun isFirstRetryInCycle(): Boolean {
    return (isRetry() && isFirstRetry()) ||
        (isTry() && isResend())
  }

  fun isTry(): Boolean {
    return numberOfRetries == 0
  }

  fun isRetry(): Boolean {
    return numberOfRetries > 0
  }
}

