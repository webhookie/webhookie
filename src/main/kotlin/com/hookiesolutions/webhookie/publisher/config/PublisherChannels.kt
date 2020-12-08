package com.hookiesolutions.webhookie.publisher.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.dsl.MessageChannels

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 9/12/20 00:12
 */
@Configuration
class PublisherChannels {
  @Bean
  fun publisherSuccessChannel(): PublishSubscribeChannel = MessageChannels.publishSubscribe().get()

  @Bean
  fun publisherResponseErrorChannel(): PublishSubscribeChannel = MessageChannels.publishSubscribe().get()

  @Bean
  fun publisherRequestErrorChannel(): PublishSubscribeChannel = MessageChannels.publishSubscribe().get()

  @Bean
  fun publisherOtherErrorChannel(): PublishSubscribeChannel = MessageChannels.publishSubscribe().get()
}