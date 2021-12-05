package com.hookiesolutions.webhookie.subscription.enterprise.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.dsl.MessageChannels
import java.util.concurrent.Executors

@Configuration
class OAuth2Channels {
  @Bean
  fun subscriptionAuthorizationErrorChannel(): PublishSubscribeChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()
}
