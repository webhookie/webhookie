package com.hookiesolutions.webhookie.subscription.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.dsl.MessageChannels
import java.util.concurrent.Executors

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/12/20 13:45
 */
@Configuration
class SubscriptionChannels {
  @Bean
  fun subscriptionChannel(): PublishSubscribeChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()

  @Bean
  fun noSubscriptionChannel(): PublishSubscribeChannel = MessageChannels
    .publishSubscribe()
    .get()

  @Bean
  fun unsuccessfulSubscriptionChannel(): PublishSubscribeChannel = MessageChannels
    .publishSubscribe()
    .get()
}