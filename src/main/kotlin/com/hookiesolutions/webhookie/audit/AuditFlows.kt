package com.hookiesolutions.webhookie.audit

import com.hookiesolutions.webhookie.common.Constants.Channels.Companion.CONSUMER_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Companion.EMPTY_SUBSCRIBER_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Companion.SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.subscription.EmptySubscriberMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import org.slf4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
import org.springframework.messaging.MessageHeaders

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 13:19
 */
@Configuration
class AuditFlows(
  private val log: Logger
) {
  @Bean
  fun logConsumerMessage(): IntegrationFlow {
    return integrationFlow {
      channel(CONSUMER_CHANNEL_NAME)
      handle { payload: ConsumerMessage, _: MessageHeaders ->
        log.warn("TOPIC: {}", payload.topic)
      }
    }
  }

  @Bean
  fun logSubscriptionMessage(): IntegrationFlow {
    return integrationFlow {
      channel(SUBSCRIPTION_CHANNEL_NAME)
      handle { payload: SubscriptionMessage, _: MessageHeaders ->
        log.info("{}", payload)
      }
    }
  }

  @Bean
  fun logEmptySubscriberMessage(): IntegrationFlow {
    return integrationFlow {
      channel(EMPTY_SUBSCRIBER_CHANNEL_NAME)
      handle { payload: EmptySubscriberMessage, _: MessageHeaders ->
        log.warn("{}", payload)
      }
    }
  }
}