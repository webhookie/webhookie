package com.hookiesolutions.webhookie.subscription.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.dsl.MessageChannels

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/12/20 13:45
 */
@Configuration("subscription-channels")
class Channels {
  class Subscribable {
    companion object {
      const val SUBSCRIPTION_CHANNEL_NAME = "subscriptionChannel"
    }
  }

  @Bean
  fun subscriptionChannel(): PublishSubscribeChannel = MessageChannels.publishSubscribe().get()
}