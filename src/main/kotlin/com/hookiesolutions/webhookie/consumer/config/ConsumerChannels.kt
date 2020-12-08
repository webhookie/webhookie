package com.hookiesolutions.webhookie.consumer.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.MessageChannels
import org.springframework.messaging.SubscribableChannel

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/12/20 13:45
 */
@Configuration
class ConsumerChannels {
  @Bean
  fun consumerChannel(): SubscribableChannel {
    return MessageChannels.publishSubscribe().get()
  }

  @Bean
  fun internalConsumerChannel(): SubscribableChannel {
    return MessageChannels.publishSubscribe().get()
  }

  @Bean
  fun missingHeadersChannel(): SubscribableChannel {
    return MessageChannels.publishSubscribe().get()
  }
}