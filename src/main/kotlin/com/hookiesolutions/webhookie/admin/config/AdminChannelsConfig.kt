package com.hookiesolutions.webhookie.admin.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.config.EnablePublisher
import org.springframework.integration.dsl.MessageChannels
import org.springframework.messaging.MessageChannel
import java.util.concurrent.Executors

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 22/1/21 15:03
 */
@Configuration
@EnablePublisher
class AdminChannelsConfig {
  @Bean
  fun consumerGroupHasBeenDeletedChannel(): MessageChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()

  @Bean
  fun consumerGroupHasBeenUpdatedChannel(): MessageChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()

  @Bean
  fun providerGroupHasBeenDeletedChannel(): MessageChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()

  @Bean
  fun providerGroupHasBeenUpdatedChannel(): MessageChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()
}