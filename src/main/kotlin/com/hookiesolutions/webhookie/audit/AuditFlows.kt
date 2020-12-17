package com.hookiesolutions.webhookie.audit

import com.hookiesolutions.webhookie.common.Constants.Channels.Consumer.Companion.CONSUMER_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Publisher.Companion.PUBLISHER_OTHER_ERROR_CHANNEL
import com.hookiesolutions.webhookie.common.Constants.Channels.Publisher.Companion.PUBLISHER_REQUEST_ERROR_CHANNEL
import com.hookiesolutions.webhookie.common.Constants.Channels.Publisher.Companion.PUBLISHER_RESPONSE_ERROR_CHANNEL
import com.hookiesolutions.webhookie.common.Constants.Channels.Publisher.Companion.PUBLISHER_SUCCESS_CHANNEL
import com.hookiesolutions.webhookie.common.Constants.Channels.Publisher.Companion.RETRY_SUBSCRIPTION_MESSAGE_CHANNEL
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.NO_SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.UNSUCCESSFUL_SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.publisher.GenericPublisherMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherOtherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherSuccessMessage
import com.hookiesolutions.webhookie.common.message.subscription.NoSubscriptionMessage
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
        log.warn("TOPIC: {}", payload.headers.topic)
      }
    }
  }

  @Bean
  fun logSubscriptionMessage(): IntegrationFlow {
    return integrationFlow {
      channel(SUBSCRIPTION_CHANNEL_NAME)
      handle { payload: SubscriptionMessage, _: MessageHeaders ->
        log.info("{}, {}, {}, {}", payload.subscription.callbackUrl, payload.delay, payload.numberOfRetries, payload.spanId)
      }
    }
  }

  @Bean
  fun logNoSubscriptionMessage(): IntegrationFlow {
    return integrationFlow {
      channel(NO_SUBSCRIPTION_CHANNEL_NAME)
      handle { payload: NoSubscriptionMessage, _: MessageHeaders ->
        log.warn("No Subscription for: '{}'", payload.originalMessage.headers.topic)
      }
    }
  }

  @Bean
  fun logPublisherSuccessMessage(): IntegrationFlow {
    return integrationFlow {
      channel(PUBLISHER_SUCCESS_CHANNEL)
      handle { payload: PublisherSuccessMessage, _: MessageHeaders ->
        log.warn("'{}', {}", payload.status, payload.subscriptionMessage.subscription.callbackUrl)
      }
    }
  }

  @Bean
  fun logPublisherRequestErrorMessage(): IntegrationFlow {
    return integrationFlow {
      channel(PUBLISHER_REQUEST_ERROR_CHANNEL)
      handle { payload: PublisherRequestErrorMessage, _: MessageHeaders ->
        log.warn("{}, {}", payload.subscriptionMessage.subscription.callbackUrl, payload.reason)
      }
    }
  }

  @Bean
  fun logPublisherResponseErrorMessage(): IntegrationFlow {
    return integrationFlow {
      channel(PUBLISHER_RESPONSE_ERROR_CHANNEL)
      handle { payload: PublisherResponseErrorMessage, _: MessageHeaders ->
        log.warn("'{}', {}, {}", payload.status, payload.subscriptionMessage.subscription.callbackUrl, payload.reason)
      }
    }
  }

  @Bean
  fun logPublisherOtherErrorMessage(): IntegrationFlow {
    return integrationFlow {
      channel(PUBLISHER_OTHER_ERROR_CHANNEL)
      handle { payload: PublisherOtherErrorMessage, _: MessageHeaders ->
        log.warn("{}, {}", payload.subscriptionMessage.subscription.callbackUrl, payload.reason)
      }
    }
  }

  @Bean
  fun logRetrySubscriptionMessageMessage(): IntegrationFlow {
    return integrationFlow {
      channel(RETRY_SUBSCRIPTION_MESSAGE_CHANNEL)
      handle { payload: GenericPublisherMessage, _: MessageHeaders ->
        log.warn("{}, {}", payload.subscriptionMessage.subscription.callbackUrl, payload.subscriptionMessage.delay.seconds)
      }
    }
  }

  @Bean
  fun logUnsuccessfulSubscriptionMessageMessage(): IntegrationFlow {
    return integrationFlow {
      channel(UNSUCCESSFUL_SUBSCRIPTION_CHANNEL_NAME)
      handle { payload: PublisherErrorMessage, _: MessageHeaders ->
        log.warn("{}, {}", payload.subscriptionMessage.subscription.callbackUrl, payload.subscriptionMessage.delay.seconds)
      }
    }
  }
}