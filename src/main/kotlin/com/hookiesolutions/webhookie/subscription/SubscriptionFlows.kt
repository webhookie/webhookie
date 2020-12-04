package com.hookiesolutions.webhookie.subscription

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.SubscriptionMessage
import com.hookiesolutions.webhookie.consumer.config.Channels.Subscribable.Companion.CONSUMER_CHANNEL_NAME
import com.hookiesolutions.webhookie.subscription.service.SubscriptionService
import org.slf4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/12/20 13:43
 */
@Configuration
class SubscriptionFlows(
  private val log: Logger,
  private val subscriptionService: SubscriptionService,
  private val subscriptionChannel: MessageChannel
) {
  @Bean
  fun subscriptionFlow(): IntegrationFlow {
    return integrationFlow {
      channel(CONSUMER_CHANNEL_NAME)
      transform<ConsumerMessage> { cm ->
        log.info("Reading '{}' subscribers", cm.topic)
        subscriptionService.findSubscriptionsFor(cm)
          .map {
            SubscriptionMessage(
              it.name,
              it.topic,
              it.callbackUrl,
              it.httpMethod,
              it.callbackSecurity,
              cm.message,
              cm.traceId,
              cm.contentType
            )
          }
      }
      split()
      channel(subscriptionChannel)
    }
  }

  @Bean
  fun listSubscribersFlow(): IntegrationFlow {
    return integrationFlow {
      channel(subscriptionChannel)
      handle { payload: Any, _: MessageHeaders ->
        log.info("{}", payload)
      }
    }
  }

}