package com.hookiesolutions.webhookie.subscription

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import org.slf4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
import org.springframework.messaging.MessageHeaders

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/12/20 13:43
 */
@Configuration
class SubscriptionFlows(
  private val log: Logger
) {
  @Bean
  fun handleSubscriptionEvent(): IntegrationFlow {
    return integrationFlow {
      channel("consumerChannel")
      handle { payload: ConsumerMessage, _: MessageHeaders ->
        log.info("{}", payload.payload)
        log.info("{}", payload.headers)
      }
    }
  }
}