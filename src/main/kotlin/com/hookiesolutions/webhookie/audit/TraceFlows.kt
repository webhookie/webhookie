/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie.audit

import com.hookiesolutions.webhookie.audit.domain.TraceSummary
import com.hookiesolutions.webhookie.audit.service.TraceService
import com.hookiesolutions.webhookie.common.Constants.Channels.Consumer.Companion.INGRESS_CHANNEL_NAME
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
      channel(INGRESS_CHANNEL_NAME)
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
