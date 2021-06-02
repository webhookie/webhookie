package com.hookiesolutions.webhookie.subscription.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.FluxMessageChannel
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
  fun delaySubscriptionChannel(): PublishSubscribeChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()

  @Bean
  fun subscriptionErrorChannel(): PublishSubscribeChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()

  @Bean
  fun globalSubscriptionErrorChannel(): PublishSubscribeChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()

  @Bean
  fun retrySubscriptionChannel(): PublishSubscribeChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()

  @Bean
  fun signSubscriptionMessageChannel(): PublishSubscribeChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()

  @Bean
  fun noSubscriptionChannel(): PublishSubscribeChannel = MessageChannels
    .publishSubscribe()
    .get()

  @Bean
  fun blockedSubscriptionChannel(): PublishSubscribeChannel = MessageChannels
    .publishSubscribe()
    .get()

  @Bean
  fun unsuccessfulSubscriptionChannel(): PublishSubscribeChannel = MessageChannels
    .publishSubscribe()
    .get()

  @Bean
  fun unblockedSubscriptionChannel(): PublishSubscribeChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()

  @Bean
  fun subscriptionActivatedChannel(): PublishSubscribeChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()

  @Bean
  fun subscriptionDeactivatedChannel(): PublishSubscribeChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()

  @Bean
  fun resendBlockedMessageChannel(): FluxMessageChannel = MessageChannels
    .flux()
    .get()
}
