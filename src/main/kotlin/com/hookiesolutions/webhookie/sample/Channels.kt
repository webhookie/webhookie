package com.hookiesolutions.webhookie.sample

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.MessageChannels
import org.springframework.messaging.MessageChannel

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 30/11/20 17:55
 */
@Configuration
class Channels {
  @Bean
  fun newPublisherChannel(): MessageChannel {
    return MessageChannels.direct().get()
  }
}