package com.hookiesolutions.webhookie.subscription

import org.slf4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow

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
      handle {
        log.info("{}", it.payload)
        log.info("{}", it.headers)
      }
    }
  }
}