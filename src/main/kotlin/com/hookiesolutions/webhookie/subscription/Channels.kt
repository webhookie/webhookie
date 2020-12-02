package com.hookiesolutions.webhookie.subscription

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.MessageChannels
import org.springframework.messaging.SubscribableChannel

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/12/20 13:45
 */
@Configuration("subscription-channels")
class Channels {
  @Bean
  fun subscriptionInChannel(): SubscribableChannel {
    return MessageChannels.publishSubscribe().get()
  }
}