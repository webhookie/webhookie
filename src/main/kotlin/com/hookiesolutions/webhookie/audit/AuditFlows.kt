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
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_SEQUENCE_SIZE
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID
import com.hookiesolutions.webhookie.common.exception.messaging.SubscriptionMessageHandlingException
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.WebhookieMessage
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
import org.springframework.integration.aggregator.CorrelationStrategy
import org.springframework.integration.aggregator.HeaderAttributeCorrelationStrategy
import org.springframework.integration.aggregator.MessageGroupProcessor
import org.springframework.integration.aggregator.ReleaseStrategy
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder

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
        log.warn("Error Occurred: '{}' for trace: {}, span: {}", payload.reason, payload.traceId, payload.spanId)
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
  fun passBlockedSubscriptionMessageToTraceAggregatorFlow(): IntegrationFlow {
    return integrationFlow {
      channel(BLOCKED_SUBSCRIPTION_CHANNEL_NAME)
      channel(TRACE_AGGREGATION_CHANNEL_NAME)
    }
  }

  @Bean
  fun passSuccessMessageToTraceAggregatorFlow(): IntegrationFlow {
    return integrationFlow {
      channel(PUBLISHER_SUCCESS_CHANNEL)
      channel(TRACE_AGGREGATION_CHANNEL_NAME)
    }
  }

  @Bean
  fun traceCorrelationStrategy(): CorrelationStrategy {
    return HeaderAttributeCorrelationStrategy(WH_HEADER_TRACE_ID)
  }

  @Bean
  fun traceReleaseStrategy(): ReleaseStrategy {
    return ReleaseStrategy {
      val size = it.one.headers[WH_HEADER_SEQUENCE_SIZE].toString().toInt()
      it.messages.size == size
    }
  }

  @Bean
  fun traceOutputProcessor(): MessageGroupProcessor {
    return MessageGroupProcessor { group ->
      val successSize = group.messages
        .filter {
          it.payload is PublisherSuccessMessage
        }
        .size

      val one = group.one.payload as WebhookieMessage
      val payload = if(group.messages.size == successSize) {
        one.traceId
      } else {
        ""
      }
      MessageBuilder
        .withPayload(payload)
        .copyHeaders(group.one.headers)
        .build()
    }
  }

  @Bean
  fun traceAggregationFlow(
    traceCorrelationStrategy: CorrelationStrategy,
    traceReleaseStrategy: ReleaseStrategy,
    traceOutputProcessor: MessageGroupProcessor
  ): IntegrationFlow {
    return integrationFlow {
      channel(TRACE_AGGREGATION_CHANNEL_NAME)
      aggregate {
        this.correlationStrategy(traceCorrelationStrategy)
        this.releaseStrategy(traceReleaseStrategy)
        this.outputProcessor(traceOutputProcessor)
      }
      filter<String> { it.isNotEmpty() }
      handle {
        trafficService.updateWithOK(it.payload as String)
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
        spanService.updateWithOtherError(payload)
      }
    }
  }

  @Bean
  fun logRetrySubscriptionMessageMessage(): IntegrationFlow {
    return integrationFlow {
      channel(RETRYABLE_PUBLISHER_ERROR_CHANNEL)
      filter<PublisherErrorMessage> { it.subscriptionMessage.numberOfRetries == 0 }
      handle { payload: PublisherErrorMessage, _: MessageHeaders ->
        trafficService.updateWithIssues(payload)
      }
    }
  }

  companion object {
    const val TRACE_AGGREGATION_CHANNEL_NAME = "traceAggregationChannel"
  }
}
