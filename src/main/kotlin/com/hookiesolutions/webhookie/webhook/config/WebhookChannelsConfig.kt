package com.hookiesolutions.webhookie.webhook.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.dsl.MessageChannels
import java.util.concurrent.Executors

@Configuration
class WebhookChannelsConfig {
  @Bean
  fun webhookApiDeletedChannel(): PublishSubscribeChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()
}
