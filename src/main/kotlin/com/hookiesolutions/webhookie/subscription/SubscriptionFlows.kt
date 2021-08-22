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

package com.hookiesolutions.webhookie.subscription

import com.hookiesolutions.webhookie.common.Constants.Channels.Consumer.Companion.CONSUMER_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Publisher.Companion.RETRYABLE_PUBLISHER_ERROR_CHANNEL
import com.hookiesolutions.webhookie.common.Constants.Channels.Traffic.Companion.TRAFFIC_RESEND_SPAN_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.HEADER_CONTENT_TYPE
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_SPAN_ID
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TOPIC
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_SEQUENCE_SIZE
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_UNBLOCKED
import com.hookiesolutions.webhookie.common.exception.messaging.SubscriptionMessageHandlingException
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.GenericSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.RetryableSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.UnsignedSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate.Keys.Companion.KEY_STATUS
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionStatus
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_STATUS_UPDATE
import com.mongodb.client.model.changestream.FullDocument
import com.mongodb.client.model.changestream.OperationType
import org.slf4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ChangeStreamOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.integration.channel.FluxMessageChannel
import org.springframework.integration.context.IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME
import org.springframework.integration.core.GenericSelector
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.mongodb.dsl.MongoDb
import org.springframework.integration.mongodb.dsl.MongoDbChangeStreamMessageProducerSpec
import org.springframework.integration.transformer.GenericTransformer
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.ErrorMessage
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/12/20 13:43
 */
@Configuration
class SubscriptionFlows(
  private val delaySubscriptionChannel: MessageChannel,
  private val blockedSubscriptionChannel: MessageChannel,
  private val signSubscriptionMessageChannel: MessageChannel,
  private val globalSubscriptionErrorChannel: MessageChannel,
  private val subscriptionChannel: MessageChannel,
  private val noSubscriptionChannel: MessageChannel,
  private val log: Logger
) {
  @Bean
  fun subscriptionFlow(
    toSubscriptionMessageFlux: GenericTransformer<ConsumerMessage, Flux<GenericSubscriptionMessage>>,
    messageHasNoSubscription: (GenericSubscriptionMessage) -> Boolean,
    subscriptionIsWorking: (GenericSubscriptionMessage) -> Boolean,
    subscriptionIsBlocked: (GenericSubscriptionMessage) -> Boolean,
    toBlockedSubscriptionMessageDTO: GenericTransformer<UnsignedSubscriptionMessage, BlockedSubscriptionMessageDTO>
  ): IntegrationFlow {
    return integrationFlow {
      channel(CONSUMER_CHANNEL_NAME)
      enrichHeaders {
        defaultOverwrite(true)
        errorChannel(globalSubscriptionErrorChannel)
      }
      transform(toSubscriptionMessageFlux)
      transform<Flux<GenericSubscriptionMessage>> { it.collectList() }
      split()
      enrichHeaders {
        headerFunction<List<GenericSubscriptionMessage>>(
          WH_HEADER_TRACE_SEQUENCE_SIZE,
          { it.payload.size },
          true)
      }
      split()
      routeToRecipients {
        applySequence(true)
        recipient(delaySubscriptionChannel, subscriptionIsWorking)
        recipient(noSubscriptionChannel, messageHasNoSubscription)
        recipientFlow(subscriptionIsBlocked) {
          transform(toBlockedSubscriptionMessageDTO)
          channel(blockedSubscriptionChannel)
        }
      }
    }
  }

  @Bean
  fun delayMessageFlow(
    nonSignableWorkingSubscription: (GenericSubscriptionMessage) -> Boolean,
    toBeSignedWorkingSubscription: (GenericSubscriptionMessage) -> Boolean,
    subscriptionIsMissing: (GenericSubscriptionMessage) -> Boolean,
    toSubscriptionMessageReloadingSubscription: GenericTransformer<RetryableSubscriptionMessage, Mono<GenericSubscriptionMessage>>,
    missingSubscriptionChannel: MessageChannel
  ): IntegrationFlow {
    return integrationFlow {
      channel(delaySubscriptionChannel)
      delay("delay-message-group") {
        this.delayFunction<RetryableSubscriptionMessage> {
          val delayInSeconds = it.payload.delay.seconds
          if(delayInSeconds > 0) {
            log.info("Delaying '{}' for '{}', traceId: '{}'", it.payload.spanId, delayInSeconds, it.payload.traceId)
          }
          delayInSeconds * 1000
        }
      }
      transform(toSubscriptionMessageReloadingSubscription)
      split()
      routeToRecipients {
        recipient(signSubscriptionMessageChannel, toBeSignedWorkingSubscription)
        recipient(subscriptionChannel, nonSignableWorkingSubscription)
        recipient(missingSubscriptionChannel, subscriptionIsMissing)
      }
    }
  }

  @Bean
  fun subscriptionErrorHandler(subscriptionErrorChannel: MessageChannel): IntegrationFlow {
    return integrationFlow {
      channel(globalSubscriptionErrorChannel)
      routeToRecipients {
        recipientFlow<ErrorMessage>({ it.payload.cause is SubscriptionMessageHandlingException }) {
          transform<ErrorMessage> { it.payload.cause as SubscriptionMessageHandlingException }
          channel(subscriptionErrorChannel)
        }
        recipientFlow<ErrorMessage>({ it.payload.cause !is SubscriptionMessageHandlingException }) {
          handle {
            log.error("Unexpected error occurred handling message: '{}', '{}", it.payload, it.headers)
          }
        }
      }
    }
  }

  @Bean
  fun retryablePublisherErrorFlow(
    requiresRetrySelector: GenericSelector<PublisherErrorMessage>,
    requiresBlockSelector: GenericSelector<PublisherErrorMessage>,
    unsuccessfulSubscriptionChannel: MessageChannel,
    retrySubscriptionChannel: MessageChannel
  ): IntegrationFlow {
    return integrationFlow {
      channel(RETRYABLE_PUBLISHER_ERROR_CHANNEL)
      routeToRecipients {
        recipient<PublisherErrorMessage>(retrySubscriptionChannel) { requiresRetrySelector.accept(it) }
        recipient<PublisherErrorMessage>(unsuccessfulSubscriptionChannel) { requiresBlockSelector.accept(it) }
        defaultOutputChannel(NULL_CHANNEL_BEAN_NAME)
      }
    }
  }

  @Bean
  fun retrySubscriptionFlow(
    retrySubscriptionChannel: MessageChannel,
    toDelayedSignableSubscriptionMessage: GenericTransformer<PublisherErrorMessage, SignableSubscriptionMessage>
  ): IntegrationFlow {
    return integrationFlow {
      channel(retrySubscriptionChannel)
      transform(toDelayedSignableSubscriptionMessage)
      channel(delaySubscriptionChannel)
    }
  }

  @Bean
  fun unsuccessfulSubscriptionFlow(
    blockSubscription: GenericTransformer<PublisherErrorMessage, Mono<BlockedSubscriptionMessageDTO>>,
    unsuccessfulSubscriptionChannel: MessageChannel,
  ): IntegrationFlow {
    return integrationFlow {
      channel(unsuccessfulSubscriptionChannel)
      transform(blockSubscription)
      split()
      channel(blockedSubscriptionChannel)
    }
  }

  @Bean
  fun blockedSubscriptionMessageFlow(
    saveBlockedMessage: GenericTransformer<BlockedSubscriptionMessageDTO, Mono<BlockedSubscriptionMessage>>
  ): IntegrationFlow {
    return integrationFlow {
      channel(blockedSubscriptionChannel)
      transform(saveBlockedMessage)
      split()
      handle{ payload: BlockedSubscriptionMessage, _: MessageHeaders ->
        log.warn("BlockedSubscriptionMessage was saved successfully: '{}'", payload.id)
      }
    }
  }

  @Bean
  fun unblockSubscriptionFlow(
    toBlockedSubscriptionMessageFlux: GenericTransformer<SubscriptionDTO, Flux<BlockedSubscriptionMessage>>,
    unblockedSubscriptionChannel: MessageChannel,
    resendBlockedMessageChannel: FluxMessageChannel
  ): IntegrationFlow {
    return integrationFlow {
      channel(unblockedSubscriptionChannel)
      transform(toBlockedSubscriptionMessageFlux)
      split()
      enrichHeaders {
        this.headerFunction<BlockedSubscriptionMessage>(WH_HEADER_TOPIC) { it.payload.consumerMessage.topic }
        this.headerFunction<BlockedSubscriptionMessage>(WH_HEADER_TRACE_ID) { it.payload.consumerMessage.traceId }
        this.headerFunction<BlockedSubscriptionMessage>(WH_HEADER_SPAN_ID) { it.payload.spanId }
        this.headerFunction<BlockedSubscriptionMessage>(WH_HEADER_UNBLOCKED) { true.toString() }
        this.headerFunction<BlockedSubscriptionMessage>(HEADER_CONTENT_TYPE) { it.payload.consumerMessage.contentType }
      }
      channel(resendBlockedMessageChannel)
    }
  }

  @Bean
  fun resendBlockedMessageFlow(
    resendBlockedMessageChannel: FluxMessageChannel,
    toSignableSubscriptionMessageReloadingSubscription: GenericTransformer<BlockedSubscriptionMessage, SignableSubscriptionMessage>,
    deleteBlockedMessage: GenericTransformer<BlockedSubscriptionMessage, Mono<BlockedSubscriptionMessage>>
  ): IntegrationFlow {
    return integrationFlow {
      channel(resendBlockedMessageChannel)
      routeToRecipients {
        recipientFlow {
          transform(toSignableSubscriptionMessageReloadingSubscription)
          channel(delaySubscriptionChannel)
        }
        recipientFlow {
          transform(deleteBlockedMessage)
          split()
          handle { bsm: BlockedSubscriptionMessage, _ ->
            log.info("Blocked Message '{}' was deleted successfully", bsm.id)
          }
        }
      }
    }
  }

  @Bean
  fun resendSpanMessageFlow() = integrationFlow {
    channel(TRAFFIC_RESEND_SPAN_CHANNEL_NAME)
    channel(delaySubscriptionChannel)
  }

  @Bean
  fun signSubscriptionFlow(
    signSubscriptionMessage: GenericTransformer<SignableSubscriptionMessage, Mono<SignableSubscriptionMessage>>,
  ): IntegrationFlow {
    return integrationFlow {
      channel(signSubscriptionMessageChannel)
      transform(signSubscriptionMessage)
      split()
      channel(subscriptionChannel)
    }
  }

}

/**
 * this is how to use it:
 *     return integrationFlow(unblockSubscriptionMongoEvent(mongoTemplate)) {
 */
@Suppress("unused")
private fun SubscriptionFlows.unblockSubscriptionMongoEvent(mongoTemplate: ReactiveMongoTemplate): MongoDbChangeStreamMessageProducerSpec {
  val key = "updateDescription.updatedFields.${KEY_STATUS_UPDATE}.${KEY_STATUS}"
  val match = Aggregation.match(
    where("operationType")
      .`is`(OperationType.UPDATE.value)
      .andOperator(where(key).`is`(SubscriptionStatus.ACTIVATED))
  )
  val changeStreamOptions = ChangeStreamOptions.builder()
    .fullDocumentLookup(FullDocument.UPDATE_LOOKUP)
    .filter(Aggregation.newAggregation(match))
    .build()
  return MongoDb
    .changeStreamInboundChannelAdapter(mongoTemplate)
    .domainType(Subscription::class.java)
    .options(changeStreamOptions)
    .extractBody(true)
}
