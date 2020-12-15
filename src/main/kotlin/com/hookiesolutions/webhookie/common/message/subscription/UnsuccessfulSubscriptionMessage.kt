package com.hookiesolutions.webhookie.common.message.subscription

import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 15/12/20 11:51
 */
data class UnsuccessfulSubscriptionMessage(
  val subscriptionMessage: SubscriptionMessage,
  val reason: String,
  val time: Instant
)
