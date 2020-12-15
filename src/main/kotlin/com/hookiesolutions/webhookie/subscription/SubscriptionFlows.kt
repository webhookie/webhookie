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
import com.hookiesolutions.webhookie.subscription.service.SubscriptionService
import org.slf4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
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
  private val timeMachine: TimeMachine,
  private val idGenerator: IdGenerator,
  private val subscriptionService: SubscriptionService,
  private val subscriptionChannel: MessageChannel,
  private val unsuccessfulSubscriptionChannel: MessageChannel,
  private val blockedSubscriptionChannel: MessageChannel,
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
        recipient<GenericSubscriptionMessage>(blockedSubscriptionChannel) { it is SubscriptionMessage && it.subscription.blockedDetails != null}
        recipient<GenericSubscriptionMessage>(subscriptionChannel) { it is SubscriptionMessage && it.subscription.blockedDetails == null}
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
  fun logBlockedSubscriptionHandler(): (BlockedSubscription, MessageHeaders) -> Unit {
    return { payload: BlockedSubscription, _: MessageHeaders ->
      log.warn("BlockedSubscriptionMessage was saved successfully: '{}'", payload.id)
    }
  }
}