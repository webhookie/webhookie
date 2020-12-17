package com.hookiesolutions.webhookie.subscription

import com.hookiesolutions.webhookie.common.Constants.Channels.Consumer.Companion.CONSUMER_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.subscription.GenericSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.NoSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import com.hookiesolutions.webhookie.common.service.IdGenerator
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_BLOCK_DETAILS
import com.hookiesolutions.webhookie.subscription.service.SubscriptionService
import com.mongodb.client.model.changestream.FullDocument
import com.mongodb.client.model.changestream.OperationType
import org.slf4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ChangeStreamOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.mongodb.dsl.MongoDb
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/12/20 13:43
 */
@Configuration
class SubscriptionFlows(
  private val log: Logger,
  private val mongoTemplate: ReactiveMongoTemplate,
  private val timeMachine: TimeMachine,
  private val idGenerator: IdGenerator,
  private val subscriptionService: SubscriptionService,
  private val subscriptionChannel: MessageChannel,
  private val unsuccessfulSubscriptionChannel: MessageChannel,
  private val blockedSubscriptionChannel: MessageChannel,
  private val resendBlockedMessageChannel: MessageChannel,
  private val noSubscriptionChannel: MessageChannel
) {
  @Bean
  fun subscriptionFlow(): IntegrationFlow {
    return integrationFlow {
      channel(CONSUMER_CHANNEL_NAME)
      transform<ConsumerMessage> { cm ->
        subscriptionService.findSubscriptionsFor(cm)
          .map { it.subscriptionMessage(cm, idGenerator.generate()) }
          .switchIfEmpty(NoSubscriptionMessage(cm).toMono())
      }
      split()
      routeToRecipients {
        recipient<GenericSubscriptionMessage>(noSubscriptionChannel) { p -> p is NoSubscriptionMessage }
        recipient<GenericSubscriptionMessage>(blockedSubscriptionChannel) { it is SubscriptionMessage && it.subscription.blockedDetails != null }
        recipient<GenericSubscriptionMessage>(subscriptionChannel) { it is SubscriptionMessage && it.subscription.blockedDetails == null }
      }
    }
  }

  @Bean
  fun unsuccessfulSubscriptionFlow(logBlockedSubscriptionHandler: (BlockedSubscriptionMessage, MessageHeaders) -> Unit): IntegrationFlow {
    return integrationFlow {
      channel(unsuccessfulSubscriptionChannel)
      transform<PublisherErrorMessage> { payload ->
        subscriptionService.blockSubscriptionFor(payload)
          .flatMap {
            subscriptionService.saveBlockedSubscription(it)
          }
      }
      split()
      handle(logBlockedSubscriptionHandler)
    }
  }

  @Bean
  fun blockedSubscriptionMessageFlow(logBlockedSubscriptionHandler: (BlockedSubscriptionMessage, MessageHeaders) -> Unit): IntegrationFlow {
    return integrationFlow {
      channel(blockedSubscriptionChannel)
      transform<SubscriptionMessage> { BlockedSubscriptionMessage.from(it, timeMachine.now(), "New Message") }
      transform<BlockedSubscriptionMessage> { subscriptionService.saveBlockedSubscription(it) }
      split()
      handle(logBlockedSubscriptionHandler)
    }
  }

  @Bean
  fun unblockSubscriptionFlow(): IntegrationFlow {
    val match = Aggregation.match(
      where("operationType")
        .`is`(OperationType.UPDATE.value)
        .and("updateDescription.removedFields")
        .regex(KEY_BLOCK_DETAILS)
    )
    val changeStreamOptions = ChangeStreamOptions.builder()
      .fullDocumentLookup(FullDocument.UPDATE_LOOKUP)
      .filter(Aggregation.newAggregation(match))
      .build()
    val producerSpec = MongoDb
      .changeStreamInboundChannelAdapter(mongoTemplate)
      .domainType(Subscription::class.java)
      .options(changeStreamOptions)
      .extractBody(true)

    return IntegrationFlows.from(producerSpec)
      .channel(resendBlockedMessageChannel)
      .get()
  }

  @Bean
  fun resendBlockedMessageFlow(): IntegrationFlow {
    return integrationFlow {
      channel(resendBlockedMessageChannel)
      transform<Subscription> { subscription ->
        subscriptionService.findAllAndRemoveBlockedMessagesForSubscription(subscription.id!!)
          .map {
            it.subscriptionMessage(idGenerator.generate())
          }
      }
      split()
      channel(subscriptionChannel)
    }
  }

  @Bean
  fun logBlockedSubscriptionHandler(): (BlockedSubscriptionMessage, MessageHeaders) -> Unit {
    return { payload: BlockedSubscriptionMessage, _: MessageHeaders ->
      log.warn("BlockedSubscriptionMessage was saved successfully: '{}'", payload.id)
    }
  }
}