package com.hookiesolutions.webhookie.subscription.exception

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 6/1/21 14:22
 */
data class SubscriptionException(
  override val message: String
): RuntimeException(message)
