package com.hookiesolutions.webhookie.subscription

import com.hookiesolutions.webhookie.common.Constants.Channels.Consumer.Companion.CONSUMER_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.subscription.GenericSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.NoSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.UnsuccessfulSubscriptionMessage
import com.hookiesolutions.webhookie.common.service.IdGenerator
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscription
import com.hookiesolutions.webhookie.subscription.domain.Company
import com.hookiesolutions.webhookie.subscription.domain.Company.Keys.Companion.KEY_COMPANY_COLLECTION_NAME
import com.hookiesolutions.webhookie.subscription.domain.Company.Keys.Companion.KEY_SUBSCRIPTIONS
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_BLOCK_DETAILS
import com.hookiesolutions.webhookie.subscription.service.SubscriptionService
import com.mongodb.client.model.changestream.FullDocument
import com.mongodb.client.model.changestream.OperationType
import org.slf4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ChangeStreamEvent
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
  fun unsuccessfulSubscriptionFlow(logBlockedSubscriptionHandler: (BlockedSubscription, MessageHeaders) -> Unit): IntegrationFlow {
    return integrationFlow {
      channel(unsuccessfulSubscriptionChannel)
      transform<UnsuccessfulSubscriptionMessage> { payload ->
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
  fun blockedSubscriptionMessageFlow(logBlockedSubscriptionHandler: (BlockedSubscription, MessageHeaders) -> Unit): IntegrationFlow {
    return integrationFlow {
      channel(blockedSubscriptionChannel)
      transform<SubscriptionMessage> { BlockedSubscription.from(it, timeMachine.now()) }
      transform<BlockedSubscription> { subscriptionService.saveBlockedSubscription(it) }
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
        .regex("$KEY_SUBSCRIPTIONS.[0-9]+.$KEY_BLOCK_DETAILS")
    )
    val changeStreamOptions = ChangeStreamOptions.builder()
      .fullDocumentLookup(FullDocument.UPDATE_LOOKUP)
      .filter(Aggregation.newAggregation(match))
      .build()
    val producerSpec = MongoDb
      .changeStreamInboundChannelAdapter(mongoTemplate)
      .domainType(Company::class.java)
      .collection(KEY_COMPANY_COLLECTION_NAME)
      .options(changeStreamOptions)
      .extractBody(false)

    return IntegrationFlows.from(producerSpec)
      .transform<ChangeStreamEvent<Company>, String> {
        it.body
          ?.findSubscriptionByUpdateRegex(it.raw?.updateDescription?.removedFields)
          ?.id
          ?: ""
      }
      .filter<String> { it.isNotBlank()}
      .channel(resendBlockedMessageChannel)
      .get()
  }

  @Bean
  fun resendBlockedMessageFlow(): IntegrationFlow {
    return integrationFlow {
      channel(resendBlockedMessageChannel)
      transform<String> { id ->
        subscriptionService.findAllAndRemoveBlockedMessagesForSubscription(id)
          .map {
            it.subscriptionMessage(idGenerator.generate())
          }
      }
      split()
      channel(subscriptionChannel)
    }
  }

  @Bean
  fun logBlockedSubscriptionHandler(): (BlockedSubscription, MessageHeaders) -> Unit {
    return { payload: BlockedSubscription, _: MessageHeaders ->
      log.warn("BlockedSubscriptionMessage was saved successfully: '{}'", payload.id)
    }
  }
}