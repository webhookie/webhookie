package com.hookiesolutions.webhookie.consumer

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.MessageChannels
import org.springframework.messaging.MessageChannel

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/12/20 13:45
 */
@Configuration("consumer-channels")
class Channels {
  @Bean
  fun eventPublisherChannel(): MessageChannel {
    return MessageChannels.direct().get()
  }
}