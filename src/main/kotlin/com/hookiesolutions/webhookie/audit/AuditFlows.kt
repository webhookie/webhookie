package com.hookiesolutions.webhookie.audit

import com.hookiesolutions.webhookie.audit.service.SpanService
import com.hookiesolutions.webhookie.audit.service.TrafficService
import com.hookiesolutions.webhookie.common.Constants.Channels.Consumer.Companion.CONSUMER_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Publisher.Companion.PUBLISHER_OTHER_ERROR_CHANNEL
import com.hookiesolutions.webhookie.common.Constants.Channels.Publisher.Companion.PUBLISHER_REQUEST_ERROR_CHANNEL
import com.hookiesolutions.webhookie.common.Constants.Channels.Publisher.Companion.PUBLISHER_RESPONSE_ERROR_CHANNEL
import com.hookiesolutions.webhookie.common.Constants.Channels.Publisher.Companion.PUBLISHER_SUCCESS_CHANNEL
import com.hookiesolutions.webhookie.common.Constants.Channels.Publisher.Companion.RETRYABLE_PUBLISHER_ERROR_CHANNEL
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.BLOCKED_SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.DELAYED_SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.NO_SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.SUBSCRIPTION_ERROR_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.UNSUCCESSFUL_SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.exception.messaging.SubscriptionMessageHandlingException
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherOtherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherSuccessMessage
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.NoSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
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
  private val log: Logger,
  private val trafficService: TrafficService,
  private val spanService: SpanService
) {
  @Bean
  fun logConsumerMessage(): IntegrationFlow {
    return integrationFlow {
      channel(CONSUMER_CHANNEL_NAME)
      handle { payload: ConsumerMessage, _: MessageHeaders ->
        trafficService.save(payload)
      }
    }
  }

  @Bean
  fun logSubscriptionMessage(): IntegrationFlow {
    return integrationFlow {
      channel(SUBSCRIPTION_CHANNEL_NAME)
      filter<SignableSubscriptionMessage> { it.numberOfRetries == 0 }
      handle { payload: SignableSubscriptionMessage, _: MessageHeaders ->
        spanService.createSpan(payload)
      }
    }
  }

  @Bean
  fun retryingSpanFlow(): IntegrationFlow {
    return integrationFlow {
      channel(DELAYED_SUBSCRIPTION_CHANNEL_NAME)
      filter<SignableSubscriptionMessage> { it.numberOfRetries > 0 }
      handle { payload: SignableSubscriptionMessage, _: MessageHeaders ->
        spanService.retrying(payload)
      }
    }
  }

  @Bean
  fun logSubscriptionErrorMessage(): IntegrationFlow {
    return integrationFlow {
      channel(SUBSCRIPTION_ERROR_CHANNEL_NAME)
      handle { payload: SubscriptionMessageHandlingException, _: MessageHeaders ->
        val h = "$SUBSCRIPTION_ERROR_CHANNEL_NAME, ${payload.traceId}, ${payload.spanId}"
        log.debug("$h - '{}'", SUBSCRIPTION_ERROR_CHANNEL_NAME, payload)
      }
    }
  }

  @Bean
  fun logBlockedSubscriptionFlow(): IntegrationFlow {
    return integrationFlow {
      channel(BLOCKED_SUBSCRIPTION_CHANNEL_NAME)
      handle { payload: BlockedSubscriptionMessageDTO, _: MessageHeaders ->
        spanService.blockSpan(payload)
      }
    }
  }

  @Bean
  fun logNoSubscriptionMessage(): IntegrationFlow {
    return integrationFlow {
      channel(NO_SUBSCRIPTION_CHANNEL_NAME)
      handle { payload: NoSubscriptionMessage, _: MessageHeaders ->
        trafficService.updateWithNoSubscription(payload)
      }
    }
  }

  @Bean
  fun logPublisherSuccessMessage(): IntegrationFlow {
    return integrationFlow {
      channel(PUBLISHER_SUCCESS_CHANNEL)
      handle { payload: PublisherSuccessMessage, _: MessageHeaders ->
        spanService.updateWithSuccessResponse(payload)
      }
    }
  }

  @Bean
  fun logPublisherRequestErrorMessage(): IntegrationFlow {
    return integrationFlow {
      channel(PUBLISHER_REQUEST_ERROR_CHANNEL)
      handle { payload: PublisherRequestErrorMessage, _: MessageHeaders ->
        spanService.updateWithClientError(payload)
      }
    }
  }

  @Bean
  fun logPublisherResponseErrorMessage(): IntegrationFlow {
    return integrationFlow {
      channel(PUBLISHER_RESPONSE_ERROR_CHANNEL)
      handle { payload: PublisherResponseErrorMessage, _: MessageHeaders ->
        spanService.updateWithServerError(payload)
      }
    }
  }

  @Bean
  fun logPublisherOtherErrorMessage(): IntegrationFlow {
    return integrationFlow {
      channel(PUBLISHER_OTHER_ERROR_CHANNEL)
      handle { payload: PublisherOtherErrorMessage, _: MessageHeaders ->
        val h = "$PUBLISHER_OTHER_ERROR_CHANNEL, ${payload.traceId}, ${payload.spanId}"
        log.debug("$h - {}, {}", payload.url, payload.reason)
      }
    }
  }

  @Bean
  fun logRetrySubscriptionMessageMessage(): IntegrationFlow {
    return integrationFlow {
      channel(RETRYABLE_PUBLISHER_ERROR_CHANNEL)
      handle { payload: PublisherErrorMessage, _: MessageHeaders ->
        val h = "$RETRYABLE_PUBLISHER_ERROR_CHANNEL, ${payload.traceId}. ${payload.spanId}"
        log.debug("$h - {}, {}", payload.url, payload.subscriptionMessage.delay.seconds)
      }
    }
  }

  @Bean
  fun logUnsuccessfulSubscriptionMessageMessage(): IntegrationFlow {
    return integrationFlow {
      channel(UNSUCCESSFUL_SUBSCRIPTION_CHANNEL_NAME)
      handle { payload: PublisherErrorMessage, _: MessageHeaders ->
        val h = "$UNSUCCESSFUL_SUBSCRIPTION_CHANNEL_NAME, ${payload.traceId}, ${payload.spanId}"
        log.warn("$h - {}, {}", payload.url, payload.subscriptionMessage.delay.seconds)
      }
    }
  }
}
