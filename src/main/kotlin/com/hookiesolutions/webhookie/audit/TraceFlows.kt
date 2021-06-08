package com.hookiesolutions.webhookie.audit

import com.hookiesolutions.webhookie.audit.domain.TraceSummary
import com.hookiesolutions.webhookie.audit.service.TraceService
import com.hookiesolutions.webhookie.common.Constants.Channels.Consumer.Companion.CONSUMER_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Publisher.Companion.PUBLISHER_SUCCESS_CHANNEL
import com.hookiesolutions.webhookie.common.Constants.Channels.Publisher.Companion.RETRYABLE_PUBLISHER_ERROR_CHANNEL
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.BLOCKED_SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.NO_SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_SEQUENCE_SIZE
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.WebhookieMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherSuccessMessage
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.NoSubscriptionMessage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.aggregator.CorrelationStrategy
import org.springframework.integration.aggregator.HeaderAttributeCorrelationStrategy
import org.springframework.integration.aggregator.MessageGroupProcessor
import org.springframework.integration.aggregator.ReleaseStrategy
import org.springframework.integration.core.MessageSelector
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import reactor.util.function.Tuple2
import reactor.util.function.Tuples

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 13:19
 */
@Configuration
class TraceFlows(private val traceService: TraceService) {
  @Bean
  fun logConsumerMessage(): IntegrationFlow {
    return integrationFlow {
      channel(CONSUMER_CHANNEL_NAME)
      handle { payload: ConsumerMessage, _: MessageHeaders ->
        traceService.save(payload)
      }
    }
  }

  @Bean
  fun updateTraceWithBlockedSubscriptionMessageFlow(): IntegrationFlow {
    return integrationFlow {
      channel(BLOCKED_SUBSCRIPTION_CHANNEL_NAME)
      handle { payload: BlockedSubscriptionMessageDTO, _: MessageHeaders ->
        traceService.updateWithIssues(payload)
      }
    }
  }

  @Bean
  fun passBlockedSubscriptionMessageToTraceAggregatorFlow(
    originalMessageSelector: MessageSelector,
    traceAggregationChannel: MessageChannel
  ): IntegrationFlow {
    return integrationFlow {
      channel(BLOCKED_SUBSCRIPTION_CHANNEL_NAME)
      filter<Message<BlockedSubscriptionMessageDTO>> {
        originalMessageSelector.accept(it)
      }
      channel(traceAggregationChannel)
    }
  }

  @Bean
  fun passSuccessMessageToTraceAggregatorFlow(
    originalMessageSelector: MessageSelector,
    unblockedMessageSelector: MessageSelector,
    resentMessageSelector: MessageSelector,
    traceAggregationChannel: MessageChannel,
    increaseSuccessChannel: MessageChannel
  ): IntegrationFlow {
    return integrationFlow {
      channel(PUBLISHER_SUCCESS_CHANNEL)
      routeToRecipients {
        recipient<Message<*>>(traceAggregationChannel) { originalMessageSelector.accept(it)}
        recipient<Message<*>>(increaseSuccessChannel) { unblockedMessageSelector.accept(it) || resentMessageSelector.accept(it)}
      }
    }
  }

  @Bean
  fun increaseSuccessFlow(
    increaseSuccessChannel: MessageChannel
  ): IntegrationFlow {
    return integrationFlow {
      channel(increaseSuccessChannel)
      handle { p: WebhookieMessage, _: MessageHeaders ->
        traceService.increaseSuccessResponse(p.traceId)
      }
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

      val payload = Tuples.of(one.traceId, TraceSummary(group.messages.size, successSize))

      MessageBuilder
        .withPayload(payload)
        .copyHeaders(group.one.headers)
        .build()
    }
  }

  @Bean
  fun traceAggregationFlow(
    traceAggregationChannel: MessageChannel,
    traceCorrelationStrategy: CorrelationStrategy,
    traceReleaseStrategy: ReleaseStrategy,
    traceOutputProcessor: MessageGroupProcessor
  ): IntegrationFlow {
    return integrationFlow {
      channel(traceAggregationChannel)
      aggregate {
        this.correlationStrategy(traceCorrelationStrategy)
        this.releaseStrategy(traceReleaseStrategy)
        this.outputProcessor(traceOutputProcessor)
      }
      handle { p: Tuple2<String, TraceSummary>, _: MessageHeaders ->
        val traceId = p.t1
        val summary = p.t2
        traceService.updateWithSummary(traceId, summary)
      }
    }
  }

  @Bean
  fun logNoSubscriptionMessage(): IntegrationFlow {
    return integrationFlow {
      channel(NO_SUBSCRIPTION_CHANNEL_NAME)
      handle { payload: NoSubscriptionMessage, _: MessageHeaders ->
        traceService.updateWithNoSubscription(payload)
      }
    }
  }

  @Bean
  fun logRetrySubscriptionMessageMessage(): IntegrationFlow {
    return integrationFlow {
      channel(RETRYABLE_PUBLISHER_ERROR_CHANNEL)
      filter<PublisherErrorMessage> { it.subscriptionMessage.numberOfRetries == 0 }
      handle { payload: PublisherErrorMessage, _: MessageHeaders ->
        traceService.updateWithIssues(payload)
      }
    }
  }
}
