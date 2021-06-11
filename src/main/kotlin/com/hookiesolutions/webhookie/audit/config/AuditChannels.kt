package com.hookiesolutions.webhookie.audit.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.MessageChannels
import org.springframework.messaging.MessageChannel
import java.util.concurrent.Executors

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 18/3/21 17:14
 */
@Configuration
class AuditChannels {
  @Bean
  fun traceAggregationChannel(): MessageChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()

  @Bean
  fun increaseSuccessChannel(): MessageChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()

  @Bean
  fun resendSpanChannel(): MessageChannel = MessageChannels
    .publishSubscribe(Executors.newCachedThreadPool())
    .get()
}
