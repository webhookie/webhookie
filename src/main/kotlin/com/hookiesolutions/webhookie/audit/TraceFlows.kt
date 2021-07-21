package com.hookiesolutions.webhookie.audit

import com.hookiesolutions.webhookie.audit.domain.TraceSummary
import com.hookiesolutions.webhookie.audit.service.TraceService
import com.hookiesolutions.webhookie.common.Constants.Channels.Consumer.Companion.CONSUMER_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Publisher.Companion.PUBLISHER_SUCCESS_CHANNEL
import com.hookiesolutions.webhookie.common.Constants.Channels.Publisher.Companion.RETRYABLE_PUBLISHER_ERROR_CHANNEL
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.BLOCKED_SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.NO_SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_SEQUENCE_SIZE
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.WebhookieMessage
import com.hookiesolutions.webhookie.common.message.publisher.GenericPublisherMessage
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
import org.springframework.integration.core.GenericSelector
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
  fun createTraceFlow(): IntegrationFlow {
    return integrationFlow {
      channel(CONSUMER_CHANNEL_NAME)
      handle { payload: ConsumerMessage, _: MessageHeaders ->
        traceService.save(payload)
      }
    }
  }

  @Bean
  fun handleBlockedSubscriptionMessageFlow(
    originalMessageSelector: MessageSelector,
    traceAggregationChannel: MessageChannel
  ): IntegrationFlow {
    return integrationFlow {
      channel(BLOCKED_SUBSCRIPTION_CHANNEL_NAME)
      routeToRecipients {
        recipient<Message<BlockedSubscriptionMessageDTO>>(traceAggregationChannel) { originalMessageSelector.accept(it) }
        recipientFlow {
          handle { payload: BlockedSubscriptionMessageDTO, _: MessageHeaders ->
            traceService.updateWithIssues(payload)
          }
        }
      }
    }
  }

  @Bean
  fun handleNonRetryableResponseFlow(
    originalMessageSelector: MessageSelector,
    retryableErrorSelector: GenericSelector<GenericPublisherMessage>,
    traceAggregationChannel: MessageChannel
  ): IntegrationFlow {
    return integrationFlow {
      channel(RETRYABLE_PUBLISHER_ERROR_CHANNEL)
      filter<Message<PublisherErrorMessage>> { !retryableErrorSelector.accept(it.payload) && originalMessageSelector.accept(it) }
      routeToRecipients {
        recipient(traceAggregationChannel)
        recipientFlow<Message<PublisherErrorMessage>>({ it.payload.subscriptionMessage.isTry() }) {
          handle { payload: PublisherErrorMessage, _: MessageHeaders ->
            traceService.updateWithIssues(payload)
          }
        }
      }
    }
  }

  @Bean
  fun handleSuccessMessageFlow(
    originalMessageSelector: MessageSelector,
    unblockedMessageSelector: MessageSelector,
    resentMessageSelector: MessageSelector,
    traceAggregationChannel: MessageChannel,
    increaseSuccessChannel: MessageChannel
  ): IntegrationFlow {
    val increaseSuccessCountSelector: (Message<*>) -> Boolean =
      { unblockedMessageSelector.accept(it) || resentMessageSelector.accept(it) }
    return integrationFlow {
      channel(PUBLISHER_SUCCESS_CHANNEL)
      routeToRecipients {
        recipient<Message<*>>(traceAggregationChannel) { originalMessageSelector.accept(it)}
        recipientFlow<Message<WebhookieMessage>>(increaseSuccessCountSelector) {
          handle { p: Message<WebhookieMessage>, _: MessageHeaders ->
            traceService.increaseSuccessResponse(p.payload.traceId)
          }
        }
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
      val size = it.one.headers[WH_HEADER_TRACE_SEQUENCE_SIZE].toString().toInt()
      it.messages.size == size
    }
  }

  @Bean
  fun traceOutputProcessor(retryableErrorSelector: GenericSelector<GenericPublisherMessage>): MessageGroupProcessor {
    return MessageGroupProcessor { group ->
      val successSize = group.messages
        .map { it.payload }
        .filterIsInstance<PublisherSuccessMessage>()
        .size

      val errorSize = group.messages
        .map { it.payload }
        .filterIsInstance<PublisherErrorMessage>()
        .size

      val blockedSize = group.messages
        .map { it.payload }
        .filterIsInstance<BlockedSubscriptionMessageDTO>()
        .size

      val one = group.one.payload as WebhookieMessage
      val numberOfSpans = group.one.headers[WH_HEADER_TRACE_SEQUENCE_SIZE].toString().toInt()
      val workingSubscriptions = numberOfSpans - blockedSize - errorSize

      val payload = Tuples.of(one.traceId,
        TraceSummary(numberOfSpans, errorSize, blockedSize, successSize, workingSubscriptions, blockedSize, errorSize))

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
  fun noSubscriptionFlow(): IntegrationFlow {
    return integrationFlow {
      channel(NO_SUBSCRIPTION_CHANNEL_NAME)
      handle { payload: NoSubscriptionMessage, _: MessageHeaders ->
        traceService.updateWithNoSubscription(payload)
      }
    }
  }
}
