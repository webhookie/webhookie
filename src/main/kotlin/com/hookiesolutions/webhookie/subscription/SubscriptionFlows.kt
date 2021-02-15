package com.hookiesolutions.webhookie.subscription

import com.hookiesolutions.webhookie.common.Constants.Channels.Consumer.Companion.CONSUMER_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Publisher.Companion.RETRYABLE_PUBLISHER_ERROR_CHANNEL
import com.hookiesolutions.webhookie.common.exception.messaging.SubscriptionMessageHandlingException
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.GenericSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignedSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.UnsignedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate.Keys.Companion.KEY_STATUS
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_STATUS_UPDATE
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionStatus
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
  private val log: Logger
) {
  @Bean
  fun subscriptionFlow(
    toSubscriptionMessageFlux: GenericTransformer<ConsumerMessage, Flux<GenericSubscriptionMessage>>,
    messageHasNoSubscription: (GenericSubscriptionMessage) -> Boolean,
    subscriptionIsBlocked: (GenericSubscriptionMessage) -> Boolean,
    toBlockedSubscriptionMessageDTO: GenericTransformer<UnsignedSubscriptionMessage, BlockedSubscriptionMessageDTO>,
    toBeSignedWorkingSubscription: (GenericSubscriptionMessage) -> Boolean,
    nonSignableWorkingSubscription: (GenericSubscriptionMessage) -> Boolean,
    signSubscriptionMessageChannel: MessageChannel,
    subscriptionChannel: MessageChannel,
    globalSubscriptionErrorChannel: MessageChannel,
    noSubscriptionChannel: MessageChannel,
    blockedSubscriptionChannel: MessageChannel
  ): IntegrationFlow {
    return integrationFlow {
      channel(CONSUMER_CHANNEL_NAME)
      enrichHeaders {
        defaultOverwrite(true)
        errorChannel(globalSubscriptionErrorChannel)
      }
      transform(toSubscriptionMessageFlux)
      split()
      routeToRecipients {
        recipient(signSubscriptionMessageChannel, toBeSignedWorkingSubscription)
        recipient(subscriptionChannel, nonSignableWorkingSubscription)
        recipient(noSubscriptionChannel, messageHasNoSubscription)
        recipientFlow(subscriptionIsBlocked, {
          transform(toBlockedSubscriptionMessageDTO)
          channel(blockedSubscriptionChannel)
        })
      }
    }
  }

  @Bean
  fun subscriptionErrorHandler(
    globalSubscriptionErrorChannel: MessageChannel,
    subscriptionErrorChannel: MessageChannel
  ): IntegrationFlow {
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
    signSubscriptionMessageChannel: MessageChannel,
    subscriptionChannel: MessageChannel,
    toDelayedSignableSubscriptionMessage: GenericTransformer<PublisherErrorMessage, SignableSubscriptionMessage>
  ): IntegrationFlow {
    return integrationFlow {
      channel(retrySubscriptionChannel)
      transform(toDelayedSignableSubscriptionMessage)
      routeToRecipients {
        recipient<SignableSubscriptionMessage>(subscriptionChannel) { it is UnsignedSubscriptionMessage}
        recipient<SignableSubscriptionMessage>(signSubscriptionMessageChannel) {it is SignedSubscriptionMessage}
      }
    }
  }

  @Bean
  fun unsuccessfulSubscriptionFlow(
    blockSubscription: GenericTransformer<PublisherErrorMessage, Mono<BlockedSubscriptionMessageDTO>>,
    unsuccessfulSubscriptionChannel: MessageChannel,
    blockedSubscriptionChannel: MessageChannel
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
    blockedSubscriptionChannel: MessageChannel,
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
    mongoTemplate: ReactiveMongoTemplate,
    toBlockedSubscriptionMessageFlux: GenericTransformer<Subscription, Flux<BlockedSubscriptionMessage>>,
    resendBlockedMessageChannel: FluxMessageChannel
  ): IntegrationFlow {
    return integrationFlow(unblockSubscriptionMongoEvent(mongoTemplate)) {
      transform(toBlockedSubscriptionMessageFlux)
      split()
      channel(resendBlockedMessageChannel)
    }
  }

  @Bean
  fun resendBlockedMessageFlow(
    resendBlockedMessageChannel: FluxMessageChannel,
    signSubscriptionMessageChannel: MessageChannel,
    subscriptionChannel: MessageChannel,
    toSignableSubscriptionMessageReloadingSubscription: GenericTransformer<BlockedSubscriptionMessage, Mono<SignableSubscriptionMessage>>,
    deleteBlockedMessage: GenericTransformer<BlockedSubscriptionMessage, Mono<BlockedSubscriptionMessage>>
  ): IntegrationFlow {
    return integrationFlow {
      channel(resendBlockedMessageChannel)
      routeToRecipients {
        recipientFlow {
          transform(toSignableSubscriptionMessageReloadingSubscription)
          split()
          routeToRecipients {
            recipient<SignableSubscriptionMessage>(subscriptionChannel) { !it.isSignable }
            recipient<SignableSubscriptionMessage>(signSubscriptionMessageChannel) { it.isSignable }
          }
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
  fun signSubscriptionFlow(
    signSubscriptionMessageChannel: MessageChannel,
    signSubscriptionMessage: GenericTransformer<SignableSubscriptionMessage, Mono<SignableSubscriptionMessage>>,
    subscriptionChannel: MessageChannel
  ): IntegrationFlow {
    return integrationFlow {
      channel(signSubscriptionMessageChannel)
      transform(signSubscriptionMessage)
      split()
      channel(subscriptionChannel)
    }
  }

  private fun unblockSubscriptionMongoEvent(
    mongoTemplate: ReactiveMongoTemplate
  ): MongoDbChangeStreamMessageProducerSpec {
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
}