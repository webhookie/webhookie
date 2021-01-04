package com.hookiesolutions.webhookie.subscription.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 9/12/20 20:40
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "webhookie.subscription")
data class SubscriptionProperties(
  val retry: RetryProfile = RetryProfile()
) {
  data class RetryProfile(
    val maxRetry: Int = 10,
    val initialInterval: Int = 10,
    val multiplier: Int = 2
  )
}

