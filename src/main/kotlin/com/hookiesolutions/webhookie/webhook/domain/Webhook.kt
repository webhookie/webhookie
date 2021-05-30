package com.hookiesolutions.webhookie.webhook.domain

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 28/5/21 15:32
 */
data class Webhook(
  val topic: Topic,
  val numberOfSubscriptions: Int = 0
) {
  class Keys {
    companion object {
      const val KEY_TOPIC = "topic"
      const val KEY_NUMBER_OF_SUBSCRIPTIONS = "numberOfSubscriptions"
    }
  }
}
